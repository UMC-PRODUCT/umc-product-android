package com.umc.domain.usecase.organization

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.organization.UpdateStudyGroupRequest
import com.umc.domain.repository.OrganizationRepository
import javax.inject.Inject

class UpdateStudyGroupUseCase @Inject constructor(
    private val organizationRepository: OrganizationRepository,
) {
    suspend operator fun invoke(
        studyGroupId: Long,
        request: UpdateStudyGroupRequest,
    ): ApiState<Unit> {
        return organizationRepository.updateStudyGroup(
            studyGroupId = studyGroupId,
            request = request,
        )
    }
}