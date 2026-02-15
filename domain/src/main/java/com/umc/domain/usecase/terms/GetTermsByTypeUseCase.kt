package com.umc.domain.usecase.terms

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.enums.TermsType
import com.umc.domain.model.terms.TermsData
import com.umc.domain.repository.terms.TermsRepository
import javax.inject.Inject

class GetTermsByTypeUseCase @Inject constructor(
    private val termsRepository: TermsRepository
) {
    suspend operator fun invoke(termsType: TermsType) : ApiState<TermsData> {
        val type = termsType.name
        return termsRepository.getTermsByType(type)
    }


}