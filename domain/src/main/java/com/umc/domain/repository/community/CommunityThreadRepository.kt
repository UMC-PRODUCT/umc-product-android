package com.umc.domain.repository.community

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.community.thread.*

interface CommunityThreadRepository {
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
