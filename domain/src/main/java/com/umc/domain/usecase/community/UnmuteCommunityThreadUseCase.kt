package com.umc.domain.usecase.community

import com.umc.domain.model.community.CommunityThreadDetail
import com.umc.domain.repository.community.CommunityThreadRepository
import javax.inject.Inject

class UnmuteCommunityThreadUseCase @Inject constructor(
    private val repository: CommunityThreadRepository,
) {
    suspend operator fun invoke(
        threadId: String,
    ): Result<CommunityThreadDetail> {
        return repository.unmuteCommunityThread(
            threadId = threadId,
        )
    }
}