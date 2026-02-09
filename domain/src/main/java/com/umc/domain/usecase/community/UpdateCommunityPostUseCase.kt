package com.umc.domain.usecase.community

import com.umc.domain.model.community.CreatePost
import com.umc.domain.repository.community.CommunityRepository
import javax.inject.Inject

class UpdateCommunityPostUseCase @Inject constructor(
    private val communityRepository: CommunityRepository
) {
    suspend operator fun invoke(postId: Long, request: CreatePost) =
        communityRepository.updatePost(postId, request)
}