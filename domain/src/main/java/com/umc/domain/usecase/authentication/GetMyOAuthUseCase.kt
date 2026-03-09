package com.umc.domain.usecase.authentication

import com.umc.domain.repository.authentication.AuthenticationRepository
import javax.inject.Inject

class GetMyOAuthUseCase @Inject constructor(
    private val authenticationRepository: AuthenticationRepository
) {
    suspend operator fun invoke() = authenticationRepository.getMyOAuth()
}