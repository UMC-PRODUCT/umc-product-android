package com.umc.data.dataSource

import com.umc.data.response.ChallengerResponse
import com.umc.domain.model.base.ApiState

interface ChallengerRemoteDataSource {
    suspend fun getChallengerDetail(id: Long): ApiState<ChallengerResponse>
}