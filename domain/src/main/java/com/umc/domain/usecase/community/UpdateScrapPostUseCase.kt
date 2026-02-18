package com.umc.domain.usecase.community

import com.umc.domain.repository.community.CommunityRepository
import javax.inject.Inject

class UpdateScrapPostUseCase @Inject constructor(
    private val communityRepository: CommunityRepository
)  {
    suspend operator fun invoke(postId: Long) =
        communityRepository.togglePostScrap(postId)
}