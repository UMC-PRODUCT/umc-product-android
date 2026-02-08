package com.umc.domain.usecase.community

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.community.ContentItem
import com.umc.domain.model.community.CreatePost
import com.umc.domain.repository.community.CommunityRepository
import javax.inject.Inject

class CreateCommunityPostUseCase @Inject constructor(
    private val communityRepository: CommunityRepository
){
    suspend operator fun invoke(request: CreatePost): ApiState<ContentItem> {
        return communityRepository.createPost(request)
    }
}
