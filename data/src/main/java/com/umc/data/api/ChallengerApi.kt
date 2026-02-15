package com.umc.data.api

import com.umc.data.response.challenger.ChallengerCursorResponse
import com.umc.data.response.challenger.ChallengerResponse
import com.umc.data.response.challenger.ChallengerSearchScheduleResponse
import com.umc.domain.model.base.ApiResponse
import com.umc.domain.model.request.challenger.ChallengerPointRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

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

    //일정 생성용 유저 검색
    @GET(Endpoints.Challenger.SEARCH_SCHEDULE)
    suspend fun searchChallengerSchedule(
        @Query("cursor") cursor: Long?,
        @Query("size") size: Int = 20,
        @Query("name") name: String?,
        @Query("nickname") nickname: String?
    ): ApiResponse<ChallengerSearchScheduleResponse>

    @GET(Endpoints.Challenger.SEARCH_CURSOR)
    suspend fun getChallengers(
        @Query("cursor") cursor: Long?,
        @Query("size") size: Int,
        @Query("part") part: String?,
        @Query("name") name: String?,
        @Query("nickname") nickname: String?,
        @Query("schoolId") schoolId: Long?,
        @Query("chapterId") chapterId: Long?,
        @Query("gisuId") gisuId: Long?
    ): ApiResponse<ChallengerCursorResponse>
}