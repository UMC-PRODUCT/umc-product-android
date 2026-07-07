package com.umc.data.api

import com.umc.data.response.EmailVerificationCompleteResponse
import com.umc.data.response.EmailVerificationResponse
import com.umc.data.response.JwtLoginResponse
import com.umc.data.response.RefreshTokenResponse
import com.umc.domain.model.base.ApiResponse
import com.umc.domain.model.request.EmailVerificationCompleteRequest
import com.umc.domain.model.request.EmailVerificationRequest
import com.umc.domain.model.request.LoginEmailRequest
import com.umc.domain.model.request.LoginGoogleRequest
import com.umc.domain.model.request.LoginRequest
import com.umc.domain.model.request.PasswordResetRequest
import com.umc.domain.model.request.RefreshTokenRequest
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST

interface AuthApi {
    @POST(Endpoints.Auth.REISSUE)
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): ApiResponse<RefreshTokenResponse>

    @POST(Endpoints.Auth.LOGIN_KAKAO)
    suspend fun loginKakao(
        @Body request: LoginRequest
    ): ApiResponse<JwtLoginResponse>

    @POST(Endpoints.Auth.LOGIN_GOOGLE)
    suspend fun loginGoogle(
        @Body request: LoginRequest
    ): ApiResponse<JwtLoginResponse>

    @POST(Endpoints.Auth.LOGIN_EMAIL)
    suspend fun loginEmail(
        @Body request: LoginEmailRequest
    ): ApiResponse<JwtLoginResponse>


    @POST(Endpoints.Auth.EMAIL_VERIFICATION)
    suspend fun emailVerification(
        @Body request: EmailVerificationRequest
    ): ApiResponse<EmailVerificationResponse>

    @POST(Endpoints.Auth.EMAIL_VERIFICATION_COMPLETE)
    suspend fun emailVerificationComplete(
        @Body request: EmailVerificationCompleteRequest
    ): ApiResponse<EmailVerificationCompleteResponse>

    //비밀번호 변경 (비밀번호 찾기)
    @PATCH(Endpoints.Auth.PASSWORD_RESET)
    suspend fun resetPassword(
        @Body request: PasswordResetRequest
    ): ApiResponse<Unit>



}