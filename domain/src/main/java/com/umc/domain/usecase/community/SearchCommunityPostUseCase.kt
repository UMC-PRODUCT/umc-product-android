package com.umc.domain.usecase.community

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.community.PostPageModel
import com.umc.domain.repository.community.CommunityRepository
import javax.inject.Inject

//게시글 검색
class SearchCommunityPostUseCase @Inject constructor(
    private val communityRepository: CommunityRepository
) {
    suspend operator fun invoke(keyword: String, page: Int, size: Int) : ApiState<PostPageModel> {
        return communityRepository.searchPosts(keyword, page, size)
    }
}