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
import com.umc.domain.model.request.LoginEmailRequest
import com.umc.domain.model.request.LoginGoogleRequest
import com.umc.domain.model.request.LoginRequest
import com.umc.domain.model.request.PasswordResetRequest
import com.umc.domain.model.request.RefreshTokenRequest
import javax.inject.Inject

class AuthRemoteDataSourceImpl @Inject constructor(
    private val authApi: AuthApi,
): AuthRemoteDataSource {

    override suspend fun reissueToken(request: RefreshTokenRequest): ApiState<RefreshTokenResponse> {
        return apiCall { authApi.refreshToken(request) }
    }

    override suspend fun loginKakao(request: LoginRequest): ApiState<JwtLoginResponse> {
        return apiCall { authApi.loginKakao(request) }
    }

    override suspend fun loginGoogle(request: LoginRequest): ApiState<JwtLoginResponse> {
        return apiCall { authApi.loginGoogle(request) }
    }

    override suspend fun loginEmail(request: LoginEmailRequest): ApiState<JwtLoginResponse> {
        return apiCall { authApi.loginEmail(request) }
    }

    override suspend fun emailVerify(request: EmailVerificationRequest): ApiState<EmailVerificationResponse> {
        return apiCall { authApi.emailVerification(request) }
    }

    override suspend fun emailVerifyComplete(request: EmailVerificationCompleteRequest): ApiState<EmailVerificationCompleteResponse> {
        return apiCall { authApi.emailVerificationComplete(request = request) }
    }

    override suspend fun resetPassword(request: PasswordResetRequest): ApiState<Unit> {
        return apiCall { authApi.resetPassword(request) }
    }

}