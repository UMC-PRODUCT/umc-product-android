package com.umc.domain.usecase.community

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.community.ContentItem
import com.umc.domain.model.community.CreateLightningPost
import com.umc.domain.repository.community.CommunityRepository
import javax.inject.Inject

//번개 게시글 작성
class CreateCommunityLightningPostUseCase @Inject constructor(
    private val communityRepository: CommunityRepository
)   {
    suspend operator fun invoke(request: CreateLightningPost): ApiState<ContentItem>{
        return communityRepository.createLightningPost(request)
    }
}