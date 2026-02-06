package com.umc.data.dataSource.remote.member

import com.umc.data.api.MemberApi
import com.umc.data.dataSource.base.apiCall
import com.umc.data.response.member.MemberResponse
import com.umc.domain.model.base.ApiResponse
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.FailState
import com.umc.domain.model.base.mapSuccessData
import javax.inject.Inject

class MemberRemoteDataSourceImpl @Inject constructor(
    private val memberApi: MemberApi
) : MemberRemoteDataSource {
    override suspend fun getMyProfile(): ApiState<MemberResponse> = fetch {
        memberApi.getMyProfile()
    }.mapSuccessData()

//    override suspend fun getMyProfile(): ApiState<MemberResponse> {
//        return apiCall { memberApi.getMyProfile() }
//    }


    override suspend fun getMemberProfile(id: Long): ApiState<MemberResponse> = fetch {
        memberApi.getMemberProfile(id)
    }.mapSuccessData()

    // 네트워크 통신의 수행 여부 체크-> 성공/실패 로직은 mappSuccessData에서 처리한다.
    private suspend fun <T> fetch(call: suspend () -> ApiResponse<T>): ApiState<ApiResponse<T>> {
        return try {
            ApiState.Success(call())
        } catch (e: Exception) {
            ApiState.Fail(FailState(false, "UNKNOWN", e.message ?: "알 수 없는 오류"))
        }
    }
}