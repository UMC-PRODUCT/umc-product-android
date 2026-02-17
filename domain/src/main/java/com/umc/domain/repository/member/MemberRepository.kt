package com.umc.domain.repository.member

import com.umc.domain.model.JwtToken
import com.umc.domain.model.UserInfo
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.member.RegisterRequest

interface MemberRepository {
    suspend fun getMyProfile(): ApiState<UserInfo>
    suspend fun getMemberProfile(id: Long): ApiState<UserInfo>
    suspend fun register(request: RegisterRequest): ApiState<JwtToken>

    suspend fun updateMyProfile(profileImageId : String): ApiState<UserInfo>
    suspend fun deleteUser(): ApiState<Unit>


}