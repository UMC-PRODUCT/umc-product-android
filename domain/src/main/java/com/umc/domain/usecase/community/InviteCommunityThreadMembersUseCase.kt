package com.umc.domain.usecase.community

import com.umc.domain.model.community.CommunityThreadInvitation
import com.umc.domain.repository.community.CommunityThreadRepository
import javax.inject.Inject

class InviteCommunityThreadMembersUseCase @Inject constructor(
    private val communityThreadRepository: CommunityThreadRepository,
) {

    suspend operator fun invoke(
        threadId: String,
        memberIds: List<Long>,
    ): Result<CommunityThreadInvitation> {
        return communityThreadRepository
            .inviteCommunityThreadMembers(
                threadId = threadId,
                memberIds = memberIds,
            )
    }
}