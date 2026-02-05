package com.umc.data.api

import com.umc.data.response.ChallengerResponse
import com.umc.domain.model.base.ApiResponse
import com.umc.domain.model.base.ApiState
import retrofit2.http.GET
import retrofit2.http.Path

interface ChallengerApi {
    @GET(Endpoints.Challenger.DETAIL)
    suspend fun getChallengerDetail(
        @Path("challengerId") challengerId: Long
    ): ApiResponse<ChallengerResponse>
}