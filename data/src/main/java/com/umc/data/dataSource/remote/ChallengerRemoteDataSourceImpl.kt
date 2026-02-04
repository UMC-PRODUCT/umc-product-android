package com.umc.data.dataSource.remote

import com.umc.data.api.ChallengerApi
import com.umc.data.dataSource.ChallengerRemoteDataSource
import com.umc.data.response.ChallengerResponse
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.FailState
import com.umc.domain.model.base.mapSuccessData
import javax.inject.Inject

class ChallengerRemoteDataSourceImpl @Inject constructor(
    private val challengerApi: ChallengerApi
) : ChallengerRemoteDataSource {
    override suspend fun getChallengerDetail(id: Long): ApiState<ChallengerResponse> {
        return try {
            val response = challengerApi.getChallengerDetail(id)
            if (response.success) {
                ApiState.Success(response.result ?: throw Exception("Data is null"))
            } else {
                ApiState.Fail(FailState(false, response.code, response.message))
            }
        } catch (e: Exception) {
            ApiState.Fail(FailState(false, "ERROR", e.message ?: "Unknown Error"))
        }
    }
}