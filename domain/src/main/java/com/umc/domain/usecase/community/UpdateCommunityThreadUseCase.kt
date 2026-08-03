package com.umc.domain.usecase.community

import com.umc.domain.model.community.CommunityThreadDetail
import com.umc.domain.repository.community.CommunityThreadRepository
import javax.inject.Inject

class UpdateCommunityThreadUseCase @Inject constructor(
    private val repository: CommunityThreadRepository,
) {
    suspend operator fun invoke(
        threadId: String,
        title: String,
        description: String,
        category: String,
        icon: String,
    ): Result<CommunityThreadDetail> {
        return repository.updateCommunityThread(
            threadId = threadId,
            title = title,
            description = description,
            category = category,
            icon = icon,
        )
    }
}