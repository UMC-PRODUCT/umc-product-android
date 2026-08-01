package com.umc.data.repository

import com.umc.data.api.CommunityThreadApi
import com.umc.data.mapper.community.toDomain
import com.umc.domain.model.community.CommunityThreadDetail
import com.umc.domain.model.community.CommunityThreadPage
import com.umc.domain.repository.CommunityThreadRepository
import javax.inject.Inject

class CommunityThreadRepositoryImpl @Inject constructor(
    private val communityThreadApi: CommunityThreadApi,
) : CommunityThreadRepository {

    override suspend fun getCommunityThreads(
        filter: String,
        query: String?,
        offset: Int,
        limit: Int,
    ): Result<CommunityThreadPage> {
        return runCatching {
            communityThreadApi.getCommunityThreads(
                filter = filter,
                query = query,
                offset = offset,
                limit = limit,
            ).toDomain()
        }
    }

    override suspend fun getCommunityThreadDetail(
        threadId: String,
    ): Result<CommunityThreadDetail> {
        return runCatching {
            communityThreadApi.getCommunityThreadDetail(
                threadId = threadId,
            ).toDomain()
        }
    }
}