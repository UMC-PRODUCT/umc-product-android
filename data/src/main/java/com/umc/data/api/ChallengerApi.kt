package com.umc.data.api

import com.umc.data.response.challenger.ChallengerResponse
import com.umc.domain.model.base.ApiResponse
import com.umc.domain.model.request.challenger.ChallengerPointRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ChallengerApi {
    @GET(Endpoints.Challenger.DETAIL)
    suspend fun getChallengerDetail(
        @Path("challengerId") challengerId: Long
    ): ApiResponse<ChallengerResponse>

    @POST(Endpoints.Challenger.POINT)
    suspend fun grantChallengerPoint(
        @Path("challengerId") challengerId: Long,
        @Body request: ChallengerPointRequest
    ): ApiResponse<ChallengerResponse>
}