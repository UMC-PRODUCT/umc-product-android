package com.umc.domain.repository.community

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.community.CommunityInvitableMemberPage
import com.umc.domain.model.community.CommunityThreadDetail
import com.umc.domain.model.community.CommunityThreadInvitation
import com.umc.domain.model.community.CommunityThreadMemberPage
import com.umc.domain.model.community.CommunityThreadPage
import com.umc.domain.model.community.thread.CommunityMessageReportReason
import com.umc.domain.model.community.thread.CommunityMessageReportReceipt
import com.umc.domain.model.community.thread.CommunityThreadDetail
import com.umc.domain.model.community.thread.CommunityThreadInvitablePage
import com.umc.domain.model.community.thread.CommunityThreadInvitation
import com.umc.domain.model.community.thread.CommunityThreadList
import com.umc.domain.model.community.thread.CommunityThreadMemberMutation
import com.umc.domain.model.community.thread.CommunityThreadMemberPage
import com.umc.domain.model.community.thread.CommunityThreadMessagePage
import com.umc.domain.model.community.thread.CommunityThreadRole
import com.umc.domain.model.community.thread.CreateCommunityThread
import com.umc.domain.model.community.thread.UpdateCommunityThread

interface CommunityThreadRepository {

    suspend fun getCommunityThreads(
        filter: String,
        query: String?,
        offset: Int,
        limit: Int,
    ): Result<CommunityThreadPage>

    suspend fun getCommunityThreadDetail(
        threadId: String,
    ): Result<CommunityThreadDetail>

    suspend fun getInvitableCommunityThreadMembers(
        threadId: String,
        query: String?,
        offset: Int,
        limit: Int,
    ): Result<CommunityInvitableMemberPage>

    suspend fun inviteCommunityThreadMembers(
        threadId: String,
        memberIds: List<Long>,
    ): Result<CommunityThreadInvitation>

    suspend fun createCommunityThread(
        title: String,
        description: String,
        category: String,
        icon: String,
        memberIds: List<Long>,
    ): Result<CommunityThreadDetail>

    suspend fun updateCommunityThread(
        threadId: String,
        title: String,
        description: String,
        category: String,
        icon: String,
    ): Result<CommunityThreadDetail>

    suspend fun leaveCommunityThread(
        threadId: String,
    ): Result<Unit>

    suspend fun deleteCommunityThread(
        threadId: String,
    ): Result<CommunityThreadDetail>

    suspend fun muteCommunityThread(
        threadId: String,
    ): Result<CommunityThreadDetail>

    suspend fun unmuteCommunityThread(
        threadId: String,
    ): Result<CommunityThreadDetail>

    suspend fun pinCommunityThread(
        threadId: String,
    ): Result<CommunityThreadDetail>

    suspend fun unpinCommunityThread(
        threadId: String,
    ): Result<CommunityThreadDetail>

    suspend fun kickCommunityThreadMember(
        threadId: String,
        memberId: String,
    ): Result<Unit>

    suspend fun getCommunityThreadMembers(
        threadId: String,
        query: String? = null,
        role: String? = null,
        part: String? = null,
        generation: Long? = null,
        offset: Int = 0,
        limit: Int = 20,
    ): Result<CommunityThreadMemberPage>

    suspend fun getThreads(filter: String = "all", query: String? = null, offset: Int = 0, limit: Int = 20): ApiState<CommunityThreadList>
    suspend fun getThread(threadId: String): ApiState<CommunityThreadDetail>
    suspend fun getMessages(threadId: String, before: String? = null, limit: Int = 30): ApiState<CommunityThreadMessagePage>
    suspend fun createThread(request: CreateCommunityThread): ApiState<CommunityThreadDetail>
    suspend fun updateThread(threadId: String, request: UpdateCommunityThread): ApiState<CommunityThreadDetail>
    suspend fun deleteThread(threadId: String): ApiState<CommunityThreadDetail>
    suspend fun setPinned(threadId: String, pinned: Boolean): ApiState<CommunityThreadDetail>
    suspend fun setMuted(threadId: String, muted: Boolean): ApiState<CommunityThreadDetail>
    suspend fun getMembers(threadId: String, query: String? = null, role: CommunityThreadRole? = null, part: String? = null, generation: Long? = null, offset: Int = 0, limit: Int = 20): ApiState<CommunityThreadMemberPage>
    suspend fun getInvitableMembers(threadId: String, query: String? = null, offset: Int = 0, limit: Int = 20): ApiState<CommunityThreadInvitablePage>
    suspend fun inviteMembers(threadId: String, memberIds: List<Long>): ApiState<CommunityThreadInvitation>
    suspend fun kickMember(threadId: String, memberId: String): ApiState<CommunityThreadMemberMutation>
    suspend fun leaveThread(threadId: String): ApiState<CommunityThreadMemberMutation>
    suspend fun changeMemberRole(threadId: String, memberId: String, role: CommunityThreadRole): ApiState<CommunityThreadMemberMutation>
    suspend fun reportMessage(messageId: String, reason: CommunityMessageReportReason): ApiState<CommunityMessageReportReceipt>
}