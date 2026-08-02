package com.umc.domain.repository.community

import com.umc.domain.model.community.CommunityInvitableMemberPage
import com.umc.domain.model.community.CommunityThreadDetail
import com.umc.domain.model.community.CommunityThreadInvitation
import com.umc.domain.model.community.CommunityThreadPage

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
}