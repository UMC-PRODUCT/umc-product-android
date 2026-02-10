package com.umc.domain.usecase.community

import com.umc.domain.repository.community.CommunityRepository
import javax.inject.Inject

//게시글의 좋아요 토글
class UpdateLikePostUseCase @Inject constructor(
    private val communityRepository: CommunityRepository
)  {
    suspend operator fun invoke(postId: Long, challengerId: Long) =
        communityRepository.togglePostLike(postId, challengerId)
}
