package com.umc.data.dataSource

import com.umc.data.response.challenger.ChallengerResponse
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.challenger.ChallengerPointRequest

interface ChallengerRemoteDataSource {
    suspend fun getChallengerDetail(id: Long): ApiState<ChallengerResponse>
    suspend fun grantChallengerPoint(id: Long, request: ChallengerPointRequest): ApiState<ChallengerResponse>
}