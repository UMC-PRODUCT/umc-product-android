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

@Singleton
class TokenAuthenticator @Inject constructor(
    private val appDataStoreRepository: AppDataStoreRepository,
    private val authRepository: AuthRepository
) : Authenticator {
    private val mutex = Mutex()

    override fun authenticate(
        route: Route?,
        response: okhttp3.Response,
    ): Request? =
        runBlocking {
            val accessToken = runBlocking { appDataStoreRepository.getAccessToken() }
            val refreshToken = runBlocking { appDataStoreRepository.getRefreshToken() }

            mutex.withLock {
                if (verifyTokenIsRefreshed(accessToken, refreshToken)) {
                    Log.d("RETROFIT", "TokenAuthenticator - authenticate() called / 중단된 API 재요청")
                    val newAccessToken = appDataStoreRepository.getAccessToken()
                    response.request
                        .newBuilder()
                        .removeHeader("Authorization")
                        .addHeader("Authorization", "Bearer $newAccessToken")
                        .build()
                } else {
                    null
                }
            }
        }

    private suspend fun verifyTokenIsRefreshed(
        access: String,
        refresh: String,
    ): Boolean {
        val newAccess = appDataStoreRepository.getAccessToken()

        if (access != newAccess) {
            return true
        }

        Log.d(
            "RETROFIT",
            "TokenAuthenticator - authenticate() called / 토큰 만료. 토큰 Refresh 요청: $refresh"
        )

        val request = RefreshTokenRequest(refresh)
        val state = authRepository.reissueToken(request)

        return when (state) {
            is ApiState.Success -> {
                appDataStoreRepository.saveTokens(state.data.accessToken, state.data.refreshToken)
                true
            }

            is ApiState.Fail -> {
                Log.d("재발급 실패", state.failState.message)
                false
            }
        }
    }
}
