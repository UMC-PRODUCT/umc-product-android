package com.umc.data.dataSource.remote.challenger

import com.umc.data.api.ChallengerApi
import com.umc.data.dataSource.base.apiCall
import com.umc.data.response.challenger.ChallengerResponse
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.challenger.ChallengerPointRequest
import javax.inject.Inject

class ChallengerRemoteDataSourceImpl @Inject constructor(
    private val challengerApi: ChallengerApi
) : ChallengerRemoteDataSource {

    override suspend fun getChallengerDetail(id: Long): ApiState<ChallengerResponse> {
        return apiCall { challengerApi.getChallengerDetail(id) }
    }

    override suspend fun grantChallengerPoint(id: Long, request: ChallengerPointRequest): ApiState<ChallengerResponse> {
        return apiCall { challengerApi.grantChallengerPoint(id, request) }
    }

}