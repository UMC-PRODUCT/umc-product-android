package com.umc.domain.usecase.community

import com.umc.domain.repository.community.CommunityRepository
import javax.inject.Inject

//게시글에 댓글을 작성하는 USECASE
class WriteCommunityPostCommentUseCase @Inject constructor(
    private val communityRepository: CommunityRepository
) {
    suspend operator fun invoke(postId: Long, challengerId: Long, content: String,
                                parentId: Long = 0L) =
        communityRepository.createComment(postId,challengerId,content,parentId)
}
