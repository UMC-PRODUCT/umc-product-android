package com.umc.data.api

import com.umc.data.base.ApiResponse
import com.umc.data.dto.request.EmailVerificationCompleteRequest
import com.umc.data.dto.request.EmailVerificationRequest
import com.umc.data.dto.request.LoginGoogleRequest
import com.umc.data.dto.request.LoginKakaoRequest
import com.umc.data.dto.request.RefreshTokenRequest
import com.umc.data.dto.response.EmailVerificationCompleteResponse
import com.umc.data.dto.response.EmailVerificationResponse
import com.umc.data.dto.response.JwtLoginResponse
import com.umc.data.dto.response.RefreshTokenResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST(Endpoints.Auth.REISSUE)
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): ApiResponse<RefreshTokenResponse>

    @POST(Endpoints.Auth.LOGIN_KAKAO)
    suspend fun loginKakao(
        @Body request: LoginKakaoRequest
    ): ApiResponse<JwtLoginResponse>

    @POST(Endpoints.Auth.LOGIN_GOOGLE)
    suspend fun loginGoogle(
        @Body request: LoginGoogleRequest
    ): ApiResponse<JwtLoginResponse>


    @POST(Endpoints.Auth.EMAIL_VERIFICATION)
    suspend fun emailVerification(
        @Body request: EmailVerificationRequest
    ): ApiResponse<EmailVerificationResponse>

    @POST(Endpoints.Auth.EMAIL_VERIFICATION_COMPLETE)
    suspend fun emailVerificationComplete(
        @Body request: EmailVerificationCompleteRequest
    ): ApiResponse<EmailVerificationCompleteResponse>

}