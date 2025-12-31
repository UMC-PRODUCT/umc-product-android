package com.umc.umc.di

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    //TODO 토큰 관련 Repository 필요
) : Authenticator {
    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: okhttp3.Response): Request? = runBlocking {
        // val access = async { repository.getAccessToken().first() }
        // val refresh = async { repository.getRefreshToken().first() }
        val accessToken = "access.await()"
        val refreshToken = "refresh.await()"

        mutex.withLock {
            if (verifyTokenIsRefreshed(accessToken, refreshToken)) {
                Log.d("RETROFIT","TokenAuthenticator - authenticate() called / 중단된 API 재요청")
                response.request
                    .newBuilder()
                    .removeHeader("Authorization")
                    .header(
                        "Authorization",
                        "Bearer " // + repository.getAccessToken().first()
                    )
                    .build()
            } else null
        }
    }

    private suspend fun verifyTokenIsRefreshed(
        access: String,
        refresh: String
    ): Boolean {
        val newAccess = "access" //repository.getAccessToken().first()

        return if (access != newAccess) true else {
            Log.d("RETROFIT","TokenAuthenticator - authenticate() called / 토큰 만료. 토큰 Refresh 요청: $refresh")
//            var foreggJwtToken = JwtResponseVo("", "")
//            JwtRepository.reIssueToken(refresh).collect { state ->
//                when(state) {
//                    is ApiState.Loading -> { }
//                    is ApiState.Success -> {
//                        JwtToken = state.data
//                        return@collect
//                    }
//                    else -> {
//                        return@collect
//                    }
//                }
//            }

//            val saveJwtRequestVo = SaveJwtRequestVo(JwtToken.accessToken, JwtToken.refreshToken)
//
//            JwtRepository.saveAccessTokenAndRefreshToken(saveJwtRequestVo).first()
//            JwtToken.isTokenValid.apply {
//                if(!this) Log.d("RETROFIT","TokenAuthenticator - verifyTokenIsRefreshed() called / 토큰 갱신 실패.")
//            }
            false //TODO 삭제
        }
    }
}