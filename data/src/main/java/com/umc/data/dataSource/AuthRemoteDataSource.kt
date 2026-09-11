package com.umc.data.dataSource

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

interface AuthRemoteDataSource {
    suspend fun reissueToken(request: RefreshTokenRequest): ApiState<RefreshTokenResponse>
    suspend fun loginKakao(request: LoginRequest): ApiState<JwtLoginResponse>
    suspend fun loginGoogle(request: LoginGoogleRequest): ApiState<JwtLoginResponse>
    suspend fun loginEmail(request: LoginEmailRequest): ApiState<JwtLoginResponse>
    suspend fun emailVerify(request: EmailVerificationRequest): ApiState<EmailVerificationResponse>
    suspend fun emailVerifyComplete(request: EmailVerificationCompleteRequest): ApiState<EmailVerificationCompleteResponse>
    suspend fun resetPassword(request: PasswordResetRequest): ApiState<Unit>
}