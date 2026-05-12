package com.umc.domain.usecase.organization

import com.umc.domain.repository.OrganizationRepository
import javax.inject.Inject

class AddStudyGroupMemberUseCase @Inject constructor(
    private val organizationRepository: OrganizationRepository
) {
    suspend operator fun invoke(studyGroupId: Long, memberId: Long) =
        organizationRepository.addStudyGroupMember(studyGroupId, memberId)
}