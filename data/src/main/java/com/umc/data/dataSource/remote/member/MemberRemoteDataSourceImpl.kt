package com.umc.data.dataSource.remote.member

import com.umc.data.api.MemberApi
import com.umc.data.dataSource.base.apiCall
import com.umc.data.response.member.MemberResponse
import com.umc.domain.model.base.ApiResponse
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.FailState
import com.umc.domain.model.base.mapSuccessData
import com.umc.domain.model.request.member.RegisterRequest
import javax.inject.Inject

class MemberRemoteDataSourceImpl @Inject constructor(
    private val memberApi: MemberApi
) : MemberRemoteDataSource {
    override suspend fun getMyProfile(): ApiState<MemberResponse> {
        return apiCall { memberApi.getMyProfile() }
    }

    override suspend fun getMemberProfile(id: Long): ApiState<MemberResponse> {
        return apiCall { memberApi.getMemberProfile(id) }
    }

    override suspend fun register(request: RegisterRequest): ApiState<Unit> {
        return apiCall { memberApi.register(request) }
    }
}