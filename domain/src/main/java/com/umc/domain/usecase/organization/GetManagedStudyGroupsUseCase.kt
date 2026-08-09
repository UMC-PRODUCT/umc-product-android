package com.umc.domain.usecase.organization

import com.umc.domain.repository.OrganizationRepository
import javax.inject.Inject

class GetManagedStudyGroupsUseCase @Inject constructor(
    private val organizationRepository: OrganizationRepository,
) {
    suspend operator fun invoke(
        cursor: Long? = null,
        size: Int = 20,
    ) = organizationRepository.getManagedStudyGroups(
        cursor = cursor,
        size = size,
    )
}