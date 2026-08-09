package com.umc.domain.usecase.community

import com.umc.domain.model.community.CommunityThreadDetail
import com.umc.domain.repository.community.CommunityThreadRepository
import javax.inject.Inject

class CreateCommunityThreadUseCase @Inject constructor(
    private val communityThreadRepository: CommunityThreadRepository,
) {

    suspend operator fun invoke(
        title: String,
        description: String,
        category: String,
        icon: String,
        memberIds: List<Long>,
    ): Result<CommunityThreadDetail> {
        return communityThreadRepository.createCommunityThread(
            title = title,
            description = description,
            category = category,
            icon = icon,
            memberIds = memberIds,
        )
    }
}