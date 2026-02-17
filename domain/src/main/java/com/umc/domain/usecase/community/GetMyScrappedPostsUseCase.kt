package com.umc.domain.usecase.community

import com.umc.domain.repository.community.CommunityRepository
import javax.inject.Inject

class GetMyScrappedPostsUseCase @Inject constructor(
    private val communityRepository: CommunityRepository
) {
    suspend operator fun invoke(page: Int, size: Int)
        = communityRepository.getMyScrappedPosts(page, size)
}
