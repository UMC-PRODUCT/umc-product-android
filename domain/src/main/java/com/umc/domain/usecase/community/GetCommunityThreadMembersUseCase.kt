package com.umc.domain.usecase.community

import com.umc.domain.model.community.CommunityThreadMemberPage
import com.umc.domain.repository.community.CommunityThreadRepository
import javax.inject.Inject

class GetCommunityThreadMembersUseCase @Inject constructor(
    private val repository: CommunityThreadRepository,
) {
    suspend operator fun invoke(
        threadId: String,
        query: String? = null,
        role: String? = null,
        part: String? = null,
        generation: Long? = null,
        offset: Int = 0,
        limit: Int = 20,
    ): Result<CommunityThreadMemberPage> {
        return repository.getCommunityThreadMembers(
            threadId = threadId,
            query = query,
            role = role,
            part = part,
            generation = generation,
            offset = offset,
            limit = limit,
        )
    }
}