package com.umc.data.dataSource.remote.community

import com.umc.data.api.CommunityApi
import com.umc.data.dataSource.base.apiCall
import com.umc.data.response.community.CommunityGetPostResponse
import com.umc.domain.model.base.ApiResponse
import com.umc.domain.model.base.ApiState
import javax.inject.Inject

class CommunityRemoteDataSourceImpl @Inject constructor(
    private val communityApi: CommunityApi
) : CommunityRemoteDataSource {

    override suspend fun getPosts(
        ing: Boolean,
        sort: String,
        page: Int,
        size: Int
    ): ApiState<CommunityGetPostResponse> {
        return apiCall { communityApi.getPosts(ing, sort, page, size) }

    }
}