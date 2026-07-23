package com.umc.domain.usecase

import com.umc.domain.model.JwtToken
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.LoginEmailRequest
import com.umc.domain.repository.AuthRepository
import javax.inject.Inject

class PostEmailLoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): ApiState<JwtToken> {
        return authRepository.emailLogin(
            LoginEmailRequest(
                email = email,
                password = password,
            )
        )
    }
}
