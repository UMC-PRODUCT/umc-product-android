package com.umc.data.dataSource.remote.community

import com.umc.data.response.community.CommunityGetPostResponse
import com.umc.domain.model.base.ApiResponse
import com.umc.domain.model.base.ApiState

interface CommunityRemoteDataSource {
    suspend fun getPosts(ing: Boolean = false, sort: String = "ALL",
                         page: Int, size: Int = 20): ApiState<CommunityGetPostResponse>
}