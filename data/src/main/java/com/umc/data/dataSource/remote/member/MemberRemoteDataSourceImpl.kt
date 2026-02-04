package com.umc.data.dataSource.remote.member

import com.umc.data.api.MemberApi
import com.umc.data.response.member.MemberResponse
import com.umc.domain.model.base.ApiResponse
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.FailState
import javax.inject.Inject

class MemberRemoteDataSourceImpl @Inject constructor(
    private val memberApi: MemberApi
) : MemberRemoteDataSource {
    override suspend fun getMyProfile(): ApiState<MemberResponse> = fetch {
        memberApi.getMyProfile()
    }

    override suspend fun getMemberProfile(id: Long): ApiState<MemberResponse> = fetch {
        memberApi.getMemberProfile(id)
    }

    // 에러 처리 로직 획일화
    private suspend fun <T> fetch(call: suspend () -> ApiResponse<T>): ApiState<T> {
        return try {
            val response = call()
            if (response.success) {
                ApiState.Success(response.result ?: throw Exception("Data is null"))
            } else {
                ApiState.Fail(FailState(false, response.code, response.message))
            }
        } catch (e: Exception) {
            ApiState.Fail(FailState(false, "UNKNOWN", e.message ?: "알 수 없는 오류"))
        }
    }
}