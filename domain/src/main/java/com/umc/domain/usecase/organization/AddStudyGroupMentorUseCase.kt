package com.umc.domain.usecase.organization

import com.umc.domain.repository.OrganizationRepository
import javax.inject.Inject

class AddStudyGroupMentorUseCase @Inject constructor(
    private val organizationRepository: OrganizationRepository
) {
    suspend operator fun invoke(studyGroupId: Long, mentorId: Long) =
        organizationRepository.addStudyGroupMentor(studyGroupId, mentorId)
}