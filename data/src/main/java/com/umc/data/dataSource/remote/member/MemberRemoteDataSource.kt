package com.umc.data.dataSource.remote.member

import com.umc.data.response.member.MemberResponse
import com.umc.domain.model.base.ApiState

interface MemberRemoteDataSource {
    suspend fun getMyProfile(): ApiState<MemberResponse>
    suspend fun getMemberProfile(id: Long): ApiState<MemberResponse>
}

