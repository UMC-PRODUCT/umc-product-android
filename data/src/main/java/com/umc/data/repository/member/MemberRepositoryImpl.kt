package com.umc.data.repository.member

import com.umc.data.dataSource.remote.member.MemberRemoteDataSource
import com.umc.data.response.member.MemberResponse.Companion.toDomain
import com.umc.domain.model.UserInfo
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.map
import com.umc.domain.repository.member.MemberRepository
import javax.inject.Inject

/**여기서 API의 response -> model의 data class로 변경**/

class MemberRepositoryImpl @Inject constructor(
    private val memberRemoteDataSource: MemberRemoteDataSource
) : MemberRepository {
    override suspend fun getMyProfile(): ApiState<UserInfo> {
        return memberRemoteDataSource.getMyProfile().map { it.toDomain() }
    }

    override suspend fun getMemberProfile(id: Long): ApiState<UserInfo> {
        return memberRemoteDataSource.getMemberProfile(id).map { it.toDomain() }
    }
}