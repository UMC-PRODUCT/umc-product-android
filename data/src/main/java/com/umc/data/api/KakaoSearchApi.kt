package com.umc.data.api

import com.umc.data.response.KakaoSearchResponse
import com.umc.domain.model.base.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface KakaoSearchApi {

    @GET(Endpoints.Kakao.SEARCH_LOCATION)
    suspend fun getKeywordSearch(
        @Query("query") query: String,
        @Query("size") size: Int = 10
    ): KakaoSearchResponse
}