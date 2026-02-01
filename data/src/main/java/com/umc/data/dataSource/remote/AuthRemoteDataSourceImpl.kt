package com.umc.data.dataSource.remote

import com.umc.data.api.AuthApi
import com.umc.data.dataSource.AuthRemoteDataSource
import com.umc.data.response.EmailVerificationCompleteResponse
import com.umc.data.response.EmailVerificationResponse
import com.umc.data.response.JwtLoginResponse
import com.umc.data.response.RefreshTokenResponse
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.mapSuccessData
import com.umc.domain.model.request.EmailVerificationCompleteRequest
import com.umc.domain.model.request.EmailVerificationRequest
import com.umc.domain.model.request.LoginGoogleRequest
import com.umc.domain.model.request.LoginKakaoRequest
import com.umc.domain.model.request.RefreshTokenRequest
import javax.inject.Inject

class AuthRemoteDataSourceImpl @Inject constructor(
    private val authApi: AuthApi,
): AuthRemoteDataSource {

    override suspend fun reissueToken(request: RefreshTokenRequest): ApiState<RefreshTokenResponse> {
        return authApi.refreshToken(request).mapSuccessData()
    }

    override suspend fun loginKakao(request: LoginKakaoRequest): ApiState<JwtLoginResponse> {
        return authApi.loginKakao(request).mapSuccessData()
    }

    override suspend fun loginGoogle(request: LoginGoogleRequest): ApiState<JwtLoginResponse> {
        return authApi.loginGoogle(request).mapSuccessData()
    }

    override suspend fun emailVerify(request: EmailVerificationRequest): ApiState<EmailVerificationResponse> {
        return authApi.emailVerification(request).mapSuccessData()
    }

    override suspend fun emailVerifyComplete(request: EmailVerificationCompleteRequest): ApiState<EmailVerificationCompleteResponse> {
        return authApi.emailVerificationComplete(request).mapSuccessData()
    }
}