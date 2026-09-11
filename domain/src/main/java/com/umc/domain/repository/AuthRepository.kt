package com.umc.domain.repository

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.JwtToken
import com.umc.domain.model.request.EmailVerificationCompleteRequest
import com.umc.domain.model.request.EmailVerificationRequest
import com.umc.domain.model.request.LoginEmailRequest
import com.umc.domain.model.request.LoginGoogleRequest
import com.umc.domain.model.request.LoginRequest
import com.umc.domain.model.request.PasswordResetRequest
import com.umc.domain.model.request.RefreshTokenRequest

interface AuthRepository {
    suspend fun reissueToken(request: RefreshTokenRequest): ApiState<JwtToken>
    suspend fun kakaoLogin(request: LoginRequest): ApiState<JwtToken>
    suspend fun googleLogin(request: LoginGoogleRequest): ApiState<JwtToken>
    suspend fun emailLogin(request: LoginEmailRequest): ApiState<JwtToken>
    suspend fun emailVerify(request: EmailVerificationRequest): ApiState<String>
    suspend fun emailVerifyComplete(request: EmailVerificationCompleteRequest): ApiState<String>
    suspend fun resetPassword(request: PasswordResetRequest): ApiState<Unit>

}