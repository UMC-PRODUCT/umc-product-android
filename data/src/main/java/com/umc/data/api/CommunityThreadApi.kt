package com.umc.data.api

import com.umc.data.request.community.CreateCommunityThreadRequest
import com.umc.data.request.community.InviteCommunityThreadMembersRequest
import com.umc.data.request.community.UpdateCommunityThreadRequest
import com.umc.data.response.community.CommunityThreadDetailResponse
import com.umc.data.response.community.CommunityThreadInvitationResponse
import com.umc.data.response.community.CommunityThreadInvitablePageResponse
import com.umc.data.response.community.CommunityThreadListResponse
import com.umc.data.response.community.CommunityThreadMemberMutationResponse
import com.umc.data.response.community.CommunityThreadMemberPageResponse
import com.umc.domain.model.base.ApiResponse
import com.umc.domain.model.community.thread.CommunityMessageReportReason
import com.umc.domain.model.community.thread.CommunityMessageReportReceipt
import com.umc.domain.model.community.thread.CommunityThreadMemberMutation
import com.umc.domain.model.community.thread.CommunityThreadMessagePage
import com.umc.domain.model.community.thread.CommunityThreadRole
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
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

    // 생성
    @POST(Endpoints.Community.THREADS)
    suspend fun createCommunityThread(
        @Body
        request: CreateCommunityThreadRequest,
    ): ApiResponse<CommunityThreadDetailResponse>

    // 스레드 정보 수정
    @PATCH(Endpoints.Community.THREAD_DETAIL)
    suspend fun updateCommunityThread(
        @Path("threadId")
        threadId: String,

        @Body
        request: UpdateCommunityThreadRequest,
    ): ApiResponse<CommunityThreadDetailResponse>

    // 스레드 나가기
    @POST(Endpoints.Community.THREAD_LEAVE)
    suspend fun leaveCommunityThread(
        @Path("threadId")
        threadId: String,
    ): ApiResponse<CommunityThreadMemberMutationResponse>

    @DELETE(Endpoints.Community.THREAD_DETAIL)
    suspend fun deleteCommunityThread(
        @Path("threadId")
        threadId: String,
    ): ApiResponse<CommunityThreadDetailResponse>

    // 알림 끄기
    @POST(Endpoints.Community.THREAD_MUTE)
    suspend fun muteCommunityThread(
        @Path("threadId")
        threadId: String,
    ): ApiResponse<CommunityThreadDetailResponse>

    // 알림 켜기
    @DELETE(Endpoints.Community.THREAD_MUTE)
    suspend fun unmuteCommunityThread(
        @Path("threadId")
        threadId: String,
    ): ApiResponse<CommunityThreadDetailResponse>

    // 고정
    @POST(Endpoints.Community.THREAD_PIN)
    suspend fun pinCommunityThread(
        @Path("threadId")
        threadId: String,
    ): ApiResponse<CommunityThreadDetailResponse>

    // 고정 해제
    @DELETE(Endpoints.Community.THREAD_PIN)
    suspend fun unpinCommunityThread(
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

    @DELETE(Endpoints.Community.THREAD_MEMBER)
    suspend fun kickCommunityThreadMember(
        @Path("threadId")
        threadId: String,
        @Path("memberId")
        memberId: String,
    ): ApiResponse<CommunityThreadMemberMutationResponse>

    @GET(Endpoints.Community.THREAD_MEMBERS)
    suspend fun getCommunityThreadMembers(
        @Path("threadId")
        threadId: String,

        @Query("q")
        query: String? = null,

        @Query("role")
        role: String? = null,

        @Query("part")
        part: String? = null,

        @Query("generation")
        generation: Long? = null,

        @Query("offset")
        offset: Int = 0,

        @Query("limit")
        limit: Int = 20,
    ): ApiResponse<CommunityThreadMemberPageResponse>

    @GET(Endpoints.Community.THREAD_MESSAGES)
    suspend fun getMessages(
        @Path("threadId") threadId: String,
        @Query("before") before: String?,
        @Query("limit") limit: Int,
    ): ApiResponse<CommunityThreadMessagePage>

    @PATCH(Endpoints.Community.THREAD_MEMBER_ROLE)
    suspend fun changeMemberRole(
        @Path("threadId") threadId: String,
        @Path("memberId") memberId: String,
        @Body request: ChangeMemberRoleRequest,
    ): ApiResponse<CommunityThreadMemberMutation>

    @POST(Endpoints.Community.MESSAGE_REPORT)
    suspend fun reportMessage(
        @Path("messageId") messageId: String,
        @Body request: ReportMessageRequest,
    ): ApiResponse<CommunityMessageReportReceipt>
}

data class ChangeMemberRoleRequest(val role: CommunityThreadRole)
data class ReportMessageRequest(val reason: CommunityMessageReportReason)
