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
}