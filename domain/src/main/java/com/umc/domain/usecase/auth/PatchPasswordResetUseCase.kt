package com.umc.domain.usecase.auth

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.PasswordResetRequest
import com.umc.domain.repository.AuthRepository
import javax.inject.Inject

class PatchPasswordResetUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(request: PasswordResetRequest): ApiState<Unit> {
        return authRepository.resetPassword(request)
    }
}
