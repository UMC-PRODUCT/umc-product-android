package com.umc.domain.usecase.community

import com.umc.domain.model.community.CommunityInvitableMemberPage
import com.umc.domain.repository.community.CommunityThreadRepository
import javax.inject.Inject

class GetInvitableCommunityThreadMembersUseCase @Inject constructor(
    private val communityThreadRepository: CommunityThreadRepository,
) {

    suspend operator fun invoke(
        threadId: String,
        query: String? = null,
        offset: Int = 0,
        limit: Int = 20,
    ): Result<CommunityInvitableMemberPage> {
        return communityThreadRepository
            .getInvitableCommunityThreadMembers(
                threadId = threadId,
                query = query,
                offset = offset,
                limit = limit,
            )
    }
}