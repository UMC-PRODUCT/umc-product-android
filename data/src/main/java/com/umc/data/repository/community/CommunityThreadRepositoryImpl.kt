package com.umc.data.repository.community

import com.umc.data.api.*
import com.umc.data.dataSource.base.apiCall
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.community.thread.*
import com.umc.domain.repository.community.CommunityThreadRepository
import javax.inject.Inject

class CommunityThreadRepositoryImpl @Inject constructor(
    private val api: CommunityThreadApi,
) : CommunityThreadRepository {
    override suspend fun getThreads(filter: String, query: String?, offset: Int, limit: Int) =
        apiCall { api.getThreads(filter, query, offset, limit) }

    override suspend fun getThread(threadId: String) = apiCall { api.getThread(threadId) }

    override suspend fun getMessages(threadId: String, before: String?, limit: Int) =
        apiCall { api.getMessages(threadId, before, limit) }

    override suspend fun createThread(request: CreateCommunityThread) =
        apiCall { api.createThread(request) }

    override suspend fun updateThread(threadId: String, request: UpdateCommunityThread) =
        apiCall { api.updateThread(threadId, request) }

    override suspend fun deleteThread(threadId: String) = apiCall { api.deleteThread(threadId) }

    override suspend fun setPinned(threadId: String, pinned: Boolean) =
        apiCall { if (pinned) api.pin(threadId) else api.unpin(threadId) }

    override suspend fun setMuted(threadId: String, muted: Boolean) =
        apiCall { if (muted) api.mute(threadId) else api.unmute(threadId) }

    override suspend fun getMembers(
        threadId: String,
        query: String?,
        role: CommunityThreadRole?,
        part: String?,
        generation: Long?,
        offset: Int,
        limit: Int,
    ) = apiCall { api.getMembers(threadId, query, role, part, generation, offset, limit) }

    override suspend fun getInvitableMembers(threadId: String, query: String?, offset: Int, limit: Int) =
        apiCall { api.getInvitableMembers(threadId, query, offset, limit) }

    override suspend fun inviteMembers(threadId: String, memberIds: List<Long>) =
        apiCall { api.inviteMembers(threadId, InviteMembersRequest(memberIds.distinct())) }

    override suspend fun kickMember(threadId: String, memberId: String) =
        apiCall { api.kickMember(threadId, memberId) }

    override suspend fun leaveThread(threadId: String) = apiCall { api.leaveThread(threadId) }

    override suspend fun changeMemberRole(threadId: String, memberId: String, role: CommunityThreadRole) =
        apiCall { api.changeMemberRole(threadId, memberId, ChangeMemberRoleRequest(role)) }

    override suspend fun reportMessage(messageId: String, reason: CommunityMessageReportReason) =
        apiCall { api.reportMessage(messageId, ReportMessageRequest(reason)) }
}
