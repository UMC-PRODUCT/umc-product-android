package com.umc.product.di

import android.util.Log
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.RefreshTokenRequest
import com.umc.domain.repository.AppDataStoreRepository
import com.umc.domain.repository.AuthRepository
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "RETROFIT"

/** 한 번의 호출에서 허용할 인증 시도 횟수. 최초 401 + 갱신 후 재시도 1회. */
private const val MAX_AUTH_ATTEMPTS = 2

@Singleton
class TokenAuthenticator @Inject constructor(
    private val appDataStoreRepository: AppDataStoreRepository,
    private val authRepository: AuthRepository
) : Authenticator {
    private val mutex = Mutex()

    override fun authenticate(
        route: Route?,
        response: okhttp3.Response,
    ): Request? {
        // 갱신된 토큰으로 재시도했는데 또 401이면 갱신으로 풀 수 있는 문제가 아니다.
        // 여기서 멈추지 않으면 renew -> 재요청 -> 401 -> renew 가 반복된다.
        if (responseCount(response) >= MAX_AUTH_ATTEMPTS) {
            Log.d(TAG, "TokenAuthenticator - 재시도 한도 초과. 인증을 포기한다")
            return null
        }

        // 실패한 요청이 실제로 들고 갔던 토큰. 저장소의 현재 값과 비교해
        // 다른 요청이 이미 갱신해 둔 상황인지 판별한다.
        val usedToken = response.request
            .header("Authorization")
            ?.removePrefix("Bearer ")
            ?.trim()
            .orEmpty()

        return runBlocking {
            mutex.withLock {
                val currentToken = appDataStoreRepository.getAccessToken()

                // 대기하는 사이 다른 요청이 갱신을 끝냈다면 그 토큰으로 바로 재시도한다.
                if (usedToken.isNotEmpty() && usedToken != currentToken && currentToken.isNotEmpty()) {
                    Log.d(TAG, "TokenAuthenticator - 다른 요청이 이미 갱신함. 새 토큰으로 재요청")
                    return@withLock response.retryWith(currentToken)
                }

                val refreshToken = appDataStoreRepository.getRefreshToken()
                if (refreshToken.isBlank()) {
                    Log.d(TAG, "TokenAuthenticator - refresh token 이 없다. 재로그인 필요")
                    appDataStoreRepository.clearTokens()
                    return@withLock null
                }

                Log.d(TAG, "TokenAuthenticator - 토큰 만료. 재발급 요청")
                // 재발급 경로에서 터진 예외가 호출부로 새어나가면 앱이 죽는다.
                // (예: success 는 true 인데 result 가 비어 온 응답)
                val state = runCatching {
                    authRepository.reissueToken(RefreshTokenRequest(refreshToken))
                }.getOrElse { e ->
                    Log.d(TAG, "TokenAuthenticator - 재발급 중 예외: ${e.message}")
                    appDataStoreRepository.clearTokens()
                    return@withLock null
                }

                when (state) {
                    is ApiState.Success -> {
                        val newAccess = runCatching { state.data.accessToken }.getOrNull().orEmpty()
                        if (newAccess.isBlank()) {
                            // 성공 응답인데 토큰이 비어 있으면 재시도해도 같은 401이 반복된다.
                            Log.d(TAG, "TokenAuthenticator - 재발급 응답에 토큰이 없다. 재로그인 필요")
                            appDataStoreRepository.clearTokens()
                            return@withLock null
                        }
                        val newRefresh = runCatching { state.data.refreshToken }.getOrNull().orEmpty()
                        appDataStoreRepository.saveTokens(newAccess, newRefresh)
                        response.retryWith(newAccess)
                    }

                    is ApiState.Fail -> {
                        // 죽은 토큰을 지운다. 남겨두면 이후 요청이 같은 토큰으로 401 -> 재발급을
                        // 되풀이하고, 화면은 세션 만료 신호를 계속 받아 재진입을 반복한다.
                        Log.d(TAG, "TokenAuthenticator - 재발급 실패(${state.failState.code}). 재로그인 필요")
                        appDataStoreRepository.clearTokens()
                        null
                    }
                }
            }
        }
    }

    private fun okhttp3.Response.retryWith(accessToken: String): Request =
        request.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()

    /** 이 응답에 이르기까지 시도된 횟수(선행 응답 포함). */
    private fun responseCount(response: okhttp3.Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
