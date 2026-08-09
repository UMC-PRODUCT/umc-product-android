package com.umc.domain.usecase.organization

import com.umc.domain.model.request.organization.CreateStudyGroupRequest
import com.umc.domain.repository.OrganizationRepository
import javax.inject.Inject

class CreateStudyGroupUseCase @Inject constructor(
    private val organizationRepository: OrganizationRepository,
) {
    suspend operator fun invoke(
        request: CreateStudyGroupRequest,
    ) = organizationRepository.createStudyGroup(
        request = request,
    )
}