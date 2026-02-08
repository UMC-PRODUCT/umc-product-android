package com.umc.domain.usecase.community

import com.umc.domain.repository.community.CommunityRepository
import javax.inject.Inject

class WriteCommunityPostCommentUseCase @Inject constructor(
    private val communityRepository: CommunityRepository
) {
    suspend operator fun invoke(postId: Long, challengerId: Long, content: String,
                                parentId: Long = 0L) =
        communityRepository.createComment(postId,challengerId,content,parentId)
}

/**
 * postId: Long, challengerId: Long, content: String, parentId: Long
 * **/