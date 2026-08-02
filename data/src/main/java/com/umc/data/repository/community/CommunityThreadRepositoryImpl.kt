package com.umc.data.repository.community

import com.umc.data.api.CommunityThreadApi
import com.umc.data.mapper.community.toDomain
import com.umc.data.request.community.CreateCommunityThreadRequest
import com.umc.data.request.community.InviteCommunityThreadMembersRequest
import com.umc.data.request.community.UpdateCommunityThreadRequest
import com.umc.domain.model.community.CommunityInvitableMemberPage
import com.umc.domain.model.community.CommunityThreadDetail
import com.umc.domain.model.community.CommunityThreadInvitation
import com.umc.domain.model.community.CommunityThreadPage
import com.umc.domain.repository.community.CommunityThreadRepository
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
            val response = communityThreadApi.getCommunityThreads(
                filter = filter,
                query = query,
                offset = offset,
                limit = limit,
            )

            requireNotNull(response.result) {
                response.message.ifBlank {
                    "스레드 목록 응답 데이터가 없어요."
                }
            }.toDomain()
        }
    }

    override suspend fun getCommunityThreadDetail(
        threadId: String,
    ): Result<CommunityThreadDetail> {
        return runCatching {
            val response = communityThreadApi.getCommunityThreadDetail(
                threadId = threadId,
            )

            requireNotNull(response.result) {
                response.message.ifBlank {
                    "스레드 상세 응답 데이터가 없어요."
                }
            }.toDomain()
        }
    }

    override suspend fun getInvitableCommunityThreadMembers(
        threadId: String,
        query: String?,
        offset: Int,
        limit: Int,
    ): Result<CommunityInvitableMemberPage> {
        return runCatching {
            val response =
                communityThreadApi.getInvitableCommunityThreadMembers(
                    threadId = threadId,
                    query = query,
                    offset = offset,
                    limit = limit,
                )

            requireNotNull(response.result) {
                response.message.ifBlank {
                    "초대 가능한 멤버 응답 데이터가 없어요."
                }
            }.toDomain()
        }
    }

    override suspend fun inviteCommunityThreadMembers(
        threadId: String,
        memberIds: List<Long>,
    ): Result<CommunityThreadInvitation> {
        return runCatching {
            val response =
                communityThreadApi.inviteCommunityThreadMembers(
                    threadId = threadId,
                    request = InviteCommunityThreadMembersRequest(
                        memberIds = memberIds,
                    ),
                )

            requireNotNull(response.result) {
                response.message.ifBlank {
                    "멤버 초대 응답 데이터가 없어요."
                }
            }.toDomain()
        }
    }

    override suspend fun createCommunityThread(
        title: String,
        description: String,
        category: String,
        icon: String,
        memberIds: List<Long>,
    ): Result<CommunityThreadDetail> {
        return runCatching {
            val response = communityThreadApi.createCommunityThread(
                request = CreateCommunityThreadRequest(
                    title = title,
                    description = description,
                    category = category,
                    icon = icon,
                    memberIds = memberIds,
                ),
            )

            requireNotNull(response.result) {
                response.message.ifBlank {
                    "스레드 생성 응답 데이터가 없어요."
                }
            }.toDomain()
        }
    }

    override suspend fun updateCommunityThread(
        threadId: String,
        title: String,
        description: String,
        category: String,
        icon: String,
    ): Result<CommunityThreadDetail> {
        return runCatching {

            val response =
                communityThreadApi.updateCommunityThread(
                    threadId = threadId,
                    request = UpdateCommunityThreadRequest(
                        title = title,
                        description = description,
                        category = category,
                        icon = icon,
                    )
                )

            requireNotNull(response.result).toDomain()
        }
    }

    override suspend fun leaveCommunityThread(
        threadId: String,
    ): Result<Unit> {
        return runCatching {

            val response =
                communityThreadApi.leaveCommunityThread(
                    threadId = threadId
                )

            requireNotNull(response.result)

            Unit
        }
    }

    override suspend fun deleteCommunityThread(
        threadId: String,
    ): Result<CommunityThreadDetail> {
        return runCatching {
            val response = communityThreadApi.deleteCommunityThread(
                threadId = threadId,
            )

            requireNotNull(response.result) {
                response.message?.takeIf { it.isNotBlank() }
                    ?: "스레드 삭제 응답 데이터가 없어요."
            }.toDomain()
        }
    }

    override suspend fun muteCommunityThread(
        threadId: String,
    ): Result<CommunityThreadDetail> {
        return runCatching {

            val response =
                communityThreadApi.muteCommunityThread(
                    threadId = threadId
                )

            requireNotNull(response.result).toDomain()
        }
    }


    override suspend fun unmuteCommunityThread(
        threadId: String,
    ): Result<CommunityThreadDetail> {
        return runCatching {

            val response =
                communityThreadApi.unmuteCommunityThread(
                    threadId = threadId
                )

            requireNotNull(response.result).toDomain()
        }
    }


    override suspend fun pinCommunityThread(
        threadId: String,
    ): Result<CommunityThreadDetail> {
        return runCatching {

            val response =
                communityThreadApi.pinCommunityThread(
                    threadId = threadId
                )

            requireNotNull(response.result).toDomain()
        }
    }

    override suspend fun unpinCommunityThread(
        threadId: String,
    ): Result<CommunityThreadDetail> {
        return runCatching {

            val response =
                communityThreadApi.unpinCommunityThread(
                    threadId = threadId
                )

            requireNotNull(response.result).toDomain()
        }
    }
}