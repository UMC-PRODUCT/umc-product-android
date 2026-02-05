package com.umc.data.dataSource.remote

import com.umc.data.api.ChallengerApi
import com.umc.data.dataSource.ChallengerRemoteDataSource
import com.umc.data.response.challenger.ChallengerResponse
import com.umc.domain.model.base.ApiResponse
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.FailState
import com.umc.domain.model.base.mapSuccessData
import com.umc.domain.model.request.challenger.ChallengerPointRequest
import javax.inject.Inject

class ChallengerRemoteDataSourceImpl @Inject constructor(
    private val challengerApi: ChallengerApi
) : ChallengerRemoteDataSource {

    override suspend fun getChallengerDetail(id: Long): ApiState<ChallengerResponse> = fetch {
        challengerApi.getChallengerDetail(id)
    }.mapSuccessData()

    override suspend fun grantChallengerPoint(id: Long, request: ChallengerPointRequest): ApiState<ChallengerResponse> = fetch {
        challengerApi.grantChallengerPoint(id, request)
    }.mapSuccessData()

    private suspend fun <T> fetch(call: suspend () -> ApiResponse<T>): ApiState<ApiResponse<T>> {
        return try {
            ApiState.Success(call())
        } catch (e: Exception) {
            ApiState.Fail(FailState(false, "UNKNOWN", e.message ?: "알 수 없는 오류"))
        }
    }
}