package com.umc.domain.repository

import com.umc.domain.model.community.CommunityThreadDetail
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
}