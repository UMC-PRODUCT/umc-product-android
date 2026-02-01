package com.umc.data.dataSource

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

interface AuthRemoteDataSource {
    suspend fun reissueToken(request: RefreshTokenRequest): ApiState<RefreshTokenResponse>
    suspend fun loginKakao(request: LoginKakaoRequest): ApiState<JwtLoginResponse>
    suspend fun loginGoogle(request: LoginGoogleRequest): ApiState<JwtLoginResponse>
    suspend fun emailVerify(request: EmailVerificationRequest): ApiState<EmailVerificationResponse>
    suspend fun emailVerifyComplete(request: EmailVerificationCompleteRequest): ApiState<EmailVerificationCompleteResponse>
}