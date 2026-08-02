package com.umc.data.api

import com.umc.data.request.community.CreateCommunityThreadRequest
import com.umc.data.request.community.InviteCommunityThreadMembersRequest
import com.umc.data.response.community.CommunityThreadDetailResponse
import com.umc.data.response.community.CommunityThreadInvitationResponse
import com.umc.data.response.community.CommunityThreadInvitablePageResponse
import com.umc.data.response.community.CommunityThreadListResponse
import com.umc.domain.model.base.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
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
    ): ApiResponse<CommunityThreadListResponse>

    @GET(Endpoints.Community.THREAD_DETAIL)
    suspend fun getCommunityThreadDetail(
        @Path("threadId")
        threadId: String,
    ): ApiResponse<CommunityThreadDetailResponse>

    @GET(Endpoints.Community.THREAD_INVITABLE)
    suspend fun getInvitableCommunityThreadMembers(
        @Path("threadId")
        threadId: String,

        @Query("q")
        query: String? = null,

        @Query("offset")
        offset: Int = 0,

        @Query("limit")
        limit: Int = 20,
    ): ApiResponse<CommunityThreadInvitablePageResponse>

    @POST(Endpoints.Community.THREAD_INVITE)
    suspend fun inviteCommunityThreadMembers(
        @Path("threadId")
        threadId: String,

        @Body
        request: InviteCommunityThreadMembersRequest,
    ): ApiResponse<CommunityThreadInvitationResponse>

    @POST(Endpoints.Community.THREADS)
    suspend fun createCommunityThread(
        @Body request: CreateCommunityThreadRequest,
    ): ApiResponse<CommunityThreadDetailResponse>
}