package com.umc.data.api

import com.umc.data.response.community.CommunityGetPostResponse
import com.umc.domain.model.base.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CommunityApi {

    @GET(Endpoints.Community.COMMUNITY)
    suspend fun getPosts(
        @Query("ing") ing: Boolean = false, // 모집 중인 번개 게시글글만 조회
        @Query("sort") sort: String = "ALL", // 정렬 기준 (SOFT:좋아요, HARD:좋아요역순, ALL:최신순)
        @Query("page") page: Int, // 현재 페이지
        @Query("size") size: Int = 20 // 페이지당 개수
    ): ApiResponse<CommunityGetPostResponse>
}