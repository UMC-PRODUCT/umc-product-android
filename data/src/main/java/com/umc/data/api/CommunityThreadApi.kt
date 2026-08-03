package com.umc.data.api

import com.umc.domain.model.base.ApiResponse
import com.umc.domain.model.community.thread.*
import retrofit2.http.*

interface CommunityThreadApi {
    @GET("api/v1/community/threads")
    suspend fun getThreads(
        @Query("filter") filter: String,
        @Query("q") query: String?,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
    ): ApiResponse<CommunityThreadList>

    @GET("api/v1/community/threads/{threadId}")
    suspend fun getThread(@Path("threadId") threadId: String): ApiResponse<CommunityThreadDetail>

    @GET("api/v1/community/threads/{threadId}/messages")
    suspend fun getMessages(
        @Path("threadId") threadId: String,
        @Query("before") before: String?,
        @Query("limit") limit: Int,
    ): ApiResponse<CommunityThreadMessagePage>

    @POST("api/v1/community/threads")
    suspend fun createThread(@Body request: CreateCommunityThread): ApiResponse<CommunityThreadDetail>

    @PATCH("api/v1/community/threads/{threadId}")
    suspend fun updateThread(
        @Path("threadId") threadId: String,
        @Body request: UpdateCommunityThread,
    ): ApiResponse<CommunityThreadDetail>

    @DELETE("api/v1/community/threads/{threadId}")
    suspend fun deleteThread(@Path("threadId") threadId: String): ApiResponse<CommunityThreadDetail>

    @POST("api/v1/community/threads/{threadId}/pin")
    suspend fun pin(@Path("threadId") threadId: String): ApiResponse<CommunityThreadDetail>

    @DELETE("api/v1/community/threads/{threadId}/pin")
    suspend fun unpin(@Path("threadId") threadId: String): ApiResponse<CommunityThreadDetail>

    @POST("api/v1/community/threads/{threadId}/mute")
    suspend fun mute(@Path("threadId") threadId: String): ApiResponse<CommunityThreadDetail>

    @DELETE("api/v1/community/threads/{threadId}/mute")
    suspend fun unmute(@Path("threadId") threadId: String): ApiResponse<CommunityThreadDetail>

    @GET("api/v1/community/threads/{threadId}/members")
    suspend fun getMembers(
        @Path("threadId") threadId: String,
        @Query("q") query: String?,
        @Query("role") role: CommunityThreadRole?,
        @Query("part") part: String?,
        @Query("generation") generation: Long?,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
    ): ApiResponse<CommunityThreadMemberPage>

    @GET("api/v1/community/threads/{threadId}/invitable")
    suspend fun getInvitableMembers(
        @Path("threadId") threadId: String,
        @Query("q") query: String?,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
    ): ApiResponse<CommunityThreadInvitablePage>

    @POST("api/v1/community/threads/{threadId}/invite")
    suspend fun inviteMembers(
        @Path("threadId") threadId: String,
        @Body request: InviteMembersRequest,
    ): ApiResponse<CommunityThreadInvitation>

    @DELETE("api/v1/community/threads/{threadId}/members/{memberId}")
    suspend fun kickMember(
        @Path("threadId") threadId: String,
        @Path("memberId") memberId: String,
    ): ApiResponse<CommunityThreadMemberMutation>

    @POST("api/v1/community/threads/{threadId}/leave")
    suspend fun leaveThread(@Path("threadId") threadId: String): ApiResponse<CommunityThreadMemberMutation>

    @PATCH("api/v1/community/threads/{threadId}/members/{memberId}/role")
    suspend fun changeMemberRole(
        @Path("threadId") threadId: String,
        @Path("memberId") memberId: String,
        @Body request: ChangeMemberRoleRequest,
    ): ApiResponse<CommunityThreadMemberMutation>

    @POST("api/v1/community/messages/{messageId}/report")
    suspend fun reportMessage(
        @Path("messageId") messageId: String,
        @Body request: ReportMessageRequest,
    ): ApiResponse<CommunityMessageReportReceipt>
}

data class InviteMembersRequest(val memberIds: List<Long>)
data class ChangeMemberRoleRequest(val role: CommunityThreadRole)
data class ReportMessageRequest(val reason: CommunityMessageReportReason)
