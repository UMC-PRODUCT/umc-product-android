package com.umc.domain.usecase

import com.umc.domain.model.JwtToken
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.enums.LoginType
import com.umc.domain.model.request.LoginGoogleRequest
import com.umc.domain.model.request.LoginKakaoRequest
import com.umc.domain.repository.AuthRepository
import javax.inject.Inject

class PostLoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(loginType: LoginType, token: String): ApiState<JwtToken> {
        return when(loginType) {
            LoginType.KAKAO -> authRepository.kakaoLogin(LoginKakaoRequest(token))
            LoginType.GOOGLE -> authRepository.googleLogin(LoginGoogleRequest(token))
        }
    }
}