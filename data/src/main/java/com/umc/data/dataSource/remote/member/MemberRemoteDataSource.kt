package com.umc.data.dataSource.remote.member

import com.umc.data.response.JwtLoginResponse
import com.umc.data.response.member.MemberResponse
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.member.RegisterRequest

interface MemberRemoteDataSource {
    suspend fun getMyProfile(): ApiState<MemberResponse>
    suspend fun getMemberProfile(id: Long): ApiState<MemberResponse>
    suspend fun register(request: RegisterRequest): ApiState<JwtLoginResponse>
}

