package com.umc.domain.usecase

import com.umc.domain.model.JwtToken
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.LoginKakaoRequest
import com.umc.domain.repository.AuthRepository
import javax.inject.Inject

class PostLoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(token: String): ApiState<JwtToken> {
        //TODO 임시 추후 구글,카카오 분기처리
        return authRepository.kakaoLogin(LoginKakaoRequest(token))
    }
}