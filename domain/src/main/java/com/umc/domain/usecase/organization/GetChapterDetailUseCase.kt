package com.umc.domain.usecase.organization

import com.umc.domain.repository.OrganizationRepository
import javax.inject.Inject

class GetChapterDetailUseCase @Inject constructor(
    private val organizationRepository: OrganizationRepository
) {
    suspend operator fun invoke(chapterId: Long) =
        organizationRepository.getChapterDetail(chapterId)
}
