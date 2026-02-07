package com.umc.data.dataSource.remote

import com.umc.data.api.AuthApi
import com.umc.data.dataSource.AuthRemoteDataSource
import com.umc.data.dataSource.base.apiCall
import com.umc.data.response.EmailVerificationCompleteResponse
import com.umc.data.response.EmailVerificationResponse
import com.umc.data.response.JwtLoginResponse
import com.umc.data.response.RefreshTokenResponse
import com.umc.domain.model.base.ApiState
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
        return apiCall { authApi.refreshToken(request) }
    }

    override suspend fun loginKakao(request: LoginKakaoRequest): ApiState<JwtLoginResponse> {
        return apiCall { authApi.loginKakao(request) }
    }

    override suspend fun loginGoogle(request: LoginGoogleRequest): ApiState<JwtLoginResponse> {
        return apiCall { authApi.loginGoogle(request) }
    }

    override suspend fun emailVerify(request: EmailVerificationRequest): ApiState<EmailVerificationResponse> {
        return apiCall { authApi.emailVerification(request) }
    }

    override suspend fun emailVerifyComplete(request: EmailVerificationCompleteRequest): ApiState<EmailVerificationCompleteResponse> {
        return apiCall { authApi.emailVerificationComplete(request) }
    }
}