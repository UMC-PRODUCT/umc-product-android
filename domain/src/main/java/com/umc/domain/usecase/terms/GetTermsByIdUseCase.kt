package com.umc.domain.usecase.terms

import com.umc.domain.repository.terms.TermsRepository
import javax.inject.Inject

class GetTermsByIdUseCase @Inject constructor(
    private val termsRepository: TermsRepository
) {
    suspend operator fun invoke(termsId: Long) = termsRepository.getTermsById(termsId)

}