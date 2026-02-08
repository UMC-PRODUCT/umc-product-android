package com.umc.domain.usecase.community

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.community.PostPageModel
import com.umc.domain.repository.community.CommunityRepository
import javax.inject.Inject

//게시글 목록들을 카테고리에 맞춰 가져오는 USECASE
class GetCommunityPostUseCase @Inject constructor(
    private val communityRepository: CommunityRepository
) {
    suspend operator fun invoke(
        category: String?,
        page: Int,
        size: Int
) : ApiState<PostPageModel> {
        return communityRepository.getPosts(category, page, size)
    }
}

