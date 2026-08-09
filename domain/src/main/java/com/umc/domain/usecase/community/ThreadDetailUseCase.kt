package com.umc.domain.usecase.community

import com.umc.domain.model.community.CommunityThreadDetail
import com.umc.domain.repository.community.CommunityThreadRepository

import javax.inject.Inject

class GetCommunityThreadDetailUseCase @Inject constructor(
    private val communityThreadRepository: CommunityThreadRepository,
) {

    suspend operator fun invoke(
        threadId: String,
    ): Result<CommunityThreadDetail> {
        return communityThreadRepository.getCommunityThreadDetail(
            threadId = threadId,
        )
    }
}