package com.umc.domain.usecase.community

import com.umc.domain.repository.community.CommunityThreadRepository
import javax.inject.Inject

class KickCommunityThreadMemberUseCase @Inject constructor(
    private val repository: CommunityThreadRepository,
) {
    suspend operator fun invoke(
        threadId: String,
        memberId: String,
    ): Result<Unit> {
        return repository.kickCommunityThreadMember(
            threadId = threadId,
            memberId = memberId,
        )
    }
}