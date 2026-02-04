package com.umc.data.dataSource.remote

import com.google.gson.Gson
import com.umc.data.api.ChallengerApi
import com.umc.data.dataSource.ChallengerRemoteDataSource
import com.umc.data.response.ChallengerResponse
import com.umc.domain.model.base.ApiResponse
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.FailState
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
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, ApiResponse::class.java)

            ApiState.Fail(FailState(
                isSuccess = false,
                code = errorResponse?.code ?: "HTTP_${e.code()}",
                message = errorResponse?.message ?: "서버 에러가 발생했습니다."
            ))
        } catch (e: Exception) {
            ApiState.Fail(FailState(false, "UNKNOWN", e.message ?: "알 수 없는 오류"))
        }
    }
}