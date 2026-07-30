package com.umc.domain.usecase.organization

import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.OrganizationRepository
import javax.inject.Inject

class DeleteStudyGroupUseCase @Inject constructor(
    private val organizationRepository: OrganizationRepository,
) {
    suspend operator fun invoke(
        studyGroupId: Long,
    ): ApiState<Unit> {
        return organizationRepository.deleteStudyGroup(
            groupId = studyGroupId,
        )
    }
}