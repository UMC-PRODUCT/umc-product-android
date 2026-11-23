package com.umc.data.api

import com.umc.data.response.challenger.ChallengerCursorResponse
import com.umc.data.response.challenger.ChallengerResponse
import com.umc.data.response.challenger.ChallengerSearchV2Response
import com.umc.domain.model.base.ApiResponse
import com.umc.domain.model.request.challenger.ChallengerPointRequest
import com.umc.domain.model.request.challenger.ChallengerRecordMemberRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ChallengerApi {
    @GET(Endpoints.Challenger.DETAIL)
    suspend fun getChallengerDetail(
        @Path("challengerId") challengerId: Long
    ): ApiResponse<ChallengerResponse>

    @GET(Endpoints.Challenger.ADMIN_DETAIL)
    suspend fun getAdminChallengerDetail(
        @Path("challengerId") challengerId: Long
    ): ApiResponse<ChallengerResponse>

    @POST(Endpoints.Challenger.POINT)
    suspend fun grantChallengerPoint(
        @Path("challengerId") challengerId: Long,
        @Body request: ChallengerPointRequest
    ): ApiResponse<ChallengerResponse>
    
    //커서를 통한 유저 검색
    @GET(Endpoints.Challenger.SEARCH)
    suspend fun getChallengers(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("part") part: String?,
        @Query("keyword") keyword: String?,
        @Query("schoolId") schoolId: Long?,
        @Query("chapterId") chapterId: Long?,
        @Query("gisuId") gisuId: Long?
    ): ApiResponse<ChallengerSearchV2Response>

    @GET(Endpoints.Challenger.ADMIN_SEARCH)
    suspend fun getAdminChallengers(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("part") part: String?,
        @Query("keyword") keyword: String?,
        @Query("schoolId") schoolId: Long?,
        @Query("chapterId") chapterId: Long?,
        @Query("gisuId") gisuId: Long
    ): ApiResponse<ChallengerSearchV2Response>

    // 본인 학교 범위의 검색에 사용하는 챌린저 단위 회원 검색.
    // 응답 모양이 v2 챌린저 검색과 같아 같은 응답 클래스로 받는다.
    @GET(Endpoints.Member.MEMBER_SEARCH)
    suspend fun searchMembers(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("part") part: String?,
        @Query("keyword") keyword: String?,
        @Query("schoolId") schoolId: Long,
        @Query("gisuId") gisuId: Long?
    ): ApiResponse<ChallengerSearchV2Response>

    @DELETE(Endpoints.Challenger.DELETE_POINT)
    suspend fun deleteChallengerPoint(
        @Path("challengerPointId") challengerPointId: Long
    ): ApiResponse<Unit>

    @POST(Endpoints.Challenger.CHALLENGER_RECORD_MEMBER)
    suspend fun addChallengerRecordMember(
        @Body request: ChallengerRecordMemberRequest
    ): ApiResponse<Unit>
}

