package com.umc.domain.usecase.community

import com.umc.domain.model.community.CommunityThreadPage
import com.umc.domain.repository.community.CommunityThreadRepository

import javax.inject.Inject

class GetCommunityThreadsUseCase @Inject constructor(
    private val communityThreadRepository: CommunityThreadRepository,
) {

    suspend operator fun invoke(
        filter: String,
        query: String? = null,
        offset: Int = 0,
        limit: Int = 20,
    ): Result<CommunityThreadPage> {
        return communityThreadRepository.getCommunityThreads(
            filter = filter,
            query = query,
            offset = offset,
            limit = limit,
        )
    }
}