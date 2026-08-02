package com.umc.domain.usecase.community

import com.umc.domain.repository.community.CommunityThreadRepository
import javax.inject.Inject

class LeaveCommunityThreadUseCase @Inject constructor(
    private val repository: CommunityThreadRepository,
) {
    suspend operator fun invoke(
        threadId: String,
    ): Result<Unit> {
        return repository.leaveCommunityThread(
            threadId = threadId,
        )
    }
}