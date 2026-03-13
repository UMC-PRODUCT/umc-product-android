package com.umc.domain.usecase.organization

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.organization.Chapter
import com.umc.domain.repository.OrganizationRepository
import javax.inject.Inject

class GetChapterListUseCase @Inject constructor(
    private val organizationRepository: OrganizationRepository
) {
    suspend operator fun invoke(): ApiState<List<Chapter>> {
        return organizationRepository.getAllChapter()
    }
}
