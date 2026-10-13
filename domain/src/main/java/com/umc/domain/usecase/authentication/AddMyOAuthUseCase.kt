package com.umc.domain.usecase.authentication

import com.umc.domain.repository.authentication.AuthenticationRepository
import javax.inject.Inject

class AddMyOAuthUseCase @Inject constructor(
    private val authenticationRepository: AuthenticationRepository
) {
    suspend operator fun invoke(oAuthVerificationToken: String) =
        authenticationRepository.addMyOAuth(oAuthVerificationToken)
}
