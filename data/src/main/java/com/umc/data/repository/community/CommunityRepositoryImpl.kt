package com.umc.data.repository.community

import com.umc.data.dataSource.remote.community.CommunityRemoteDataSource
import com.umc.data.response.community.CommunityGetPostResponse.Companion.toCommunityDomain
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.map
import com.umc.domain.model.community.PostPageModel
import com.umc.domain.repository.community.CommunityRepository
import javax.inject.Inject

class CommunityRepositoryImpl @Inject constructor(
    private val communityRemoteDataSource: CommunityRemoteDataSource
) : CommunityRepository {

    override suspend fun getPosts(
        ing: Boolean,
        sort: String,
        page: Int,
        size: Int
    ): ApiState<PostPageModel> {
        return communityRemoteDataSource.getPosts(ing, sort, page, size).map {
            it.toCommunityDomain() }


    }
}