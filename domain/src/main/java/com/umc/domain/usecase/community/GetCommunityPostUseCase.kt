package com.umc.domain.usecase.community

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.community.PostPageModel
import com.umc.domain.repository.community.CommunityRepository
import javax.inject.Inject

class GetCommunityPostUseCase @Inject constructor(
    private val communityRepository: CommunityRepository
) {
    suspend operator fun invoke(
        ing: Boolean,
        sort: String,
        page: Int,
        size: Int
) : ApiState<PostPageModel> {
        return communityRepository.getPosts(ing, sort, page, size)
    }
}

