package com.umc.data.repository

import com.umc.data.dataSource.AuthRemoteDataSource
import com.umc.data.response.EmailVerificationCompleteResponse.Companion.toModel
import com.umc.data.response.EmailVerificationResponse.Companion.toModel
import com.umc.data.response.JwtLoginResponse.Companion.toModel
import com.umc.data.response.RefreshTokenResponse.Companion.toModel
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.JwtToken
import com.umc.domain.model.base.map
import com.umc.domain.model.request.EmailVerificationCompleteRequest
import com.umc.domain.model.request.EmailVerificationRequest
import com.umc.domain.model.request.LoginGoogleRequest
import com.umc.domain.model.request.LoginKakaoRequest
import com.umc.domain.model.request.RefreshTokenRequest
import com.umc.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authRemoteDataSource: AuthRemoteDataSource
) : AuthRepository {
    override suspend fun reissueToken(request: RefreshTokenRequest): ApiState<String> {
        return authRemoteDataSource.reissueToken(request).map {
            it.toModel()
        }
    }

    override suspend fun kakaoLogin(request: LoginKakaoRequest): ApiState<JwtToken> {
        return authRemoteDataSource.loginKakao(request).map {
            it.toModel()
        }
    }

    override suspend fun kakaoGoogle(request: LoginGoogleRequest): ApiState<JwtToken> {
        return authRemoteDataSource.loginGoogle(request).map {
            it.toModel()
        }
    }

    override suspend fun emailVerify(request: EmailVerificationRequest): ApiState<String> {
        return authRemoteDataSource.emailVerify(request).map {
            it.toModel()
        }
    }

    override suspend fun emailVerifyComplete(request: EmailVerificationCompleteRequest): ApiState<String> {
        return authRemoteDataSource.emailVerifyComplete(request).map {
            it.toModel()
        }
    }
}