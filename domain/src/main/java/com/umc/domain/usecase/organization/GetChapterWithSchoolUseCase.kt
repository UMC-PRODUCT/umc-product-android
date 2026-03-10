package com.umc.domain.usecase.organization

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.organization.ChapterWithSchool
import com.umc.domain.repository.OrganizationRepository
import javax.inject.Inject

class GetChapterWithSchoolUseCase @Inject constructor(
    private val organizationRepository: OrganizationRepository
) {
    suspend operator fun invoke(gisuId: Int): ApiState<ChapterWithSchool> {
        return organizationRepository.getChapterWithSchool(gisuId)
    }
}
