package com.umc.domain.usecase.auth

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.EmailVerificationRequest
import com.umc.domain.repository.AuthRepository
import javax.inject.Inject

class PostEmailVerificationUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(request: EmailVerificationRequest): ApiState<String> {
        return authRepository.emailVerify(request)
    }
}