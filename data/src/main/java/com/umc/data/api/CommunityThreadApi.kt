package com.umc.data.api

import com.umc.data.response.community.CommunityThreadDetailResponse
import com.umc.data.response.community.CommunityThreadListResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CommunityThreadApi {

    @GET(Endpoints.Community.THREADS)
    suspend fun getCommunityThreads(
        @Query("filter")
        filter: String = "all",

        @Query("q")
        query: String? = null,

        @Query("offset")
        offset: Int = 0,

        @Query("limit")
        limit: Int = 20,
    ): CommunityThreadListResponse

    @GET(Endpoints.Community.THREAD_DETAIL)
    suspend fun getCommunityThreadDetail(
        @Path("threadId")
        threadId: String,
    ): CommunityThreadDetailResponse
}