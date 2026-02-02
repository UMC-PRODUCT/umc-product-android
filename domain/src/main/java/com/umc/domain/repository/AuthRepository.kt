package com.umc.domain.repository

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.JwtToken
import com.umc.domain.model.request.EmailVerificationCompleteRequest
import com.umc.domain.model.request.EmailVerificationRequest
import com.umc.domain.model.request.LoginGoogleRequest
import com.umc.domain.model.request.LoginKakaoRequest
import com.umc.domain.model.request.RefreshTokenRequest

interface AuthRepository {
    suspend fun reissueToken(request: RefreshTokenRequest): ApiState<String>
    suspend fun kakaoLogin(request: LoginKakaoRequest): ApiState<JwtToken>
    suspend fun kakaoGoogle(request: LoginGoogleRequest): ApiState<JwtToken>
    suspend fun emailVerify(request: EmailVerificationRequest): ApiState<String>
    suspend fun emailVerifyComplete(request: EmailVerificationCompleteRequest): ApiState<String>
}