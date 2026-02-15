package com.umc.domain.usecase.community

import com.umc.domain.repository.community.CommunityRepository
import javax.inject.Inject

//내 게시글 가져오는 usecase
class GetMyPostsUseCase @Inject constructor(
    private val communityRepository: CommunityRepository
) {
    suspend operator fun invoke(
        page: Int,
        size: Int
    ) = communityRepository.getMyPosts(page, size)
}