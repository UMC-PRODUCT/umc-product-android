package com.umc.data.dataSource.remote.member

import com.umc.data.api.MemberApi
import com.umc.data.dataSource.base.apiCall
import com.umc.data.request.member.DeleteUserRequest
import com.umc.data.request.member.UpdateMyLinkRequest
import com.umc.data.request.member.UpdateMyProfileRequest
import com.umc.data.response.JwtLoginResponse
import com.umc.data.response.member.MemberResponse
import com.umc.domain.model.base.ApiState
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

    override suspend fun register(request: RegisterRequest): ApiState<JwtLoginResponse> {
        return apiCall { memberApi.register(request) }
    }

    override suspend fun updateMyProfile(request: UpdateMyProfileRequest): ApiState<MemberResponse> {
        return apiCall { memberApi.updateMyProfile(request) }
    }

    override suspend fun updateMyLink(request: UpdateMyLinkRequest): ApiState<MemberResponse> {
        return apiCall { memberApi.updateMyLink(request) }
    }

    override suspend fun deleteUser(request: DeleteUserRequest): ApiState<Unit> {
        return apiCall { memberApi.deleteUser(request) }
    }
}