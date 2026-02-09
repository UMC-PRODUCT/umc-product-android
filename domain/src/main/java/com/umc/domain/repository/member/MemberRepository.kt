package com.umc.domain.repository.member

import com.umc.domain.model.UserInfo
import com.umc.domain.model.base.ApiState

interface MemberRepository {
    suspend fun getMyProfile(): ApiState<UserInfo>
    suspend fun getMemberProfile(id: Long): ApiState<UserInfo>
}