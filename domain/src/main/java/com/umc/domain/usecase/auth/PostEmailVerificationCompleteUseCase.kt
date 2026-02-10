package com.umc.domain.usecase.auth

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.EmailVerificationCompleteRequest
import com.umc.domain.repository.AuthRepository
import javax.inject.Inject

class PostEmailVerificationCompleteUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(request: EmailVerificationCompleteRequest): ApiState<String> {
        return authRepository.emailVerifyComplete(request)
    }
}