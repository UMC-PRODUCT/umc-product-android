package com.umc.domain.usecase.community

import com.umc.domain.model.community.CreateLightningPost
import com.umc.domain.repository.community.CommunityRepository
import javax.inject.Inject


//번개글 업데이트
class UpdateCommunityLightningPostUseCase @Inject constructor(
    private val communityRepository: CommunityRepository
){
    suspend operator fun invoke(postId: Long, request: CreateLightningPost) =
        communityRepository.updateLightningPost(postId, request)
}