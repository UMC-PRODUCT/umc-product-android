package com.umc.domain.usecase.organization

import com.umc.domain.repository.OrganizationRepository
import javax.inject.Inject

class GetSearchGisusUseCase @Inject constructor(private val repository: OrganizationRepository) {
    suspend operator fun invoke() = repository.getSearchGisus()
}
