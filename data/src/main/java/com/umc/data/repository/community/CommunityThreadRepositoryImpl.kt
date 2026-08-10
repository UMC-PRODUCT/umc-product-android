package com.umc.data.repository.community

import com.umc.data.api.ChangeMemberRoleRequest
import com.umc.data.api.CommunityThreadApi
import com.umc.data.api.ReportMessageRequest
import com.umc.data.dataSource.base.apiCall
import com.umc.data.mapper.community.toChatDomain
import com.umc.data.mapper.community.toDomain
import com.umc.data.request.community.CreateCommunityThreadRequest
import com.umc.data.request.community.InviteCommunityThreadMembersRequest
import com.umc.data.request.community.UpdateCommunityThreadRequest
import com.umc.domain.model.community.CommunityInvitableMemberPage
import com.umc.domain.model.community.CommunityThreadDetail
import com.umc.domain.model.community.CommunityThreadInvitation
import com.umc.domain.model.community.CommunityThreadMemberPage
import com.umc.domain.model.community.CommunityThreadPage
import com.umc.domain.model.community.thread.CommunityMessageReportReason
import com.umc.domain.model.community.thread.CommunityThreadRole
import com.umc.domain.model.community.thread.CreateCommunityThread
import com.umc.domain.model.community.thread.UpdateCommunityThread
import com.umc.domain.model.base.map
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

    override suspend fun kickCommunityThreadMember(
        threadId: String,
        memberId: String,
    ): Result<Unit> {
        return runCatching {
            val response =
                communityThreadApi.kickCommunityThreadMember(
                    threadId = threadId,
                    memberId = memberId,
                )

            requireNotNull(response.result) {
                response.message.ifBlank {
                    "멤버 삭제 응답 데이터가 없어요."
                }
            }

            Unit
        }
    }

    override suspend fun getCommunityThreadMembers(
        threadId: String,
        query: String?,
        role: String?,
        part: String?,
        generation: Long?,
        offset: Int,
        limit: Int,
    ): Result<CommunityThreadMemberPage> {
        return runCatching {
            val response =
                communityThreadApi.getCommunityThreadMembers(
                    threadId = threadId,
                    query = query,
                    role = role,
                    part = part,
                    generation = generation,
                    offset = offset,
                    limit = limit,
                )

            requireNotNull(response.result) {
                response.message.ifBlank {
                    "스레드 멤버 응답 데이터가 없어요."
                }
            }.toDomain()
        }
    }

    override suspend fun getThreads(filter: String, query: String?, offset: Int, limit: Int) =
        apiCall {
            communityThreadApi.getCommunityThreads(filter, query, offset, limit)
        }.map { it.toChatDomain() }

    override suspend fun getThread(threadId: String) =
        apiCall { communityThreadApi.getCommunityThreadDetail(threadId) }
            .map { it.toChatDomain() }

    override suspend fun getMessages(threadId: String, before: String?, limit: Int) =
        apiCall { communityThreadApi.getMessages(threadId, before, limit) }

    override suspend fun createThread(request: CreateCommunityThread) =
        apiCall {
            communityThreadApi.createCommunityThread(
                CreateCommunityThreadRequest(
                    title = request.title,
                    description = request.description,
                    category = request.category.name,
                    icon = request.icon,
                    memberIds = request.memberIds,
                )
            )
        }.map { it.toChatDomain() }

    override suspend fun updateThread(threadId: String, request: UpdateCommunityThread) =
        apiCall {
            communityThreadApi.updateCommunityThread(
                threadId = threadId,
                request = UpdateCommunityThreadRequest(
                    title = request.title,
                    description = request.description,
                    category = request.category?.name,
                    icon = request.icon,
                ),
            )
        }.map { it.toChatDomain() }

    override suspend fun deleteThread(threadId: String) =
        apiCall { communityThreadApi.deleteCommunityThread(threadId) }
            .map { it.toChatDomain() }

    override suspend fun setPinned(threadId: String, pinned: Boolean) =
        apiCall {
            if (pinned) {
                communityThreadApi.pinCommunityThread(threadId)
            } else {
                communityThreadApi.unpinCommunityThread(threadId)
            }
        }.map { it.toChatDomain() }

    override suspend fun setMuted(threadId: String, muted: Boolean) =
        apiCall {
            if (muted) {
                communityThreadApi.muteCommunityThread(threadId)
            } else {
                communityThreadApi.unmuteCommunityThread(threadId)
            }
        }.map { it.toChatDomain() }

    override suspend fun getMembers(
        threadId: String,
        query: String?,
        role: CommunityThreadRole?,
        part: String?,
        generation: Long?,
        offset: Int,
        limit: Int,
    ) = apiCall {
        communityThreadApi.getCommunityThreadMembers(
            threadId = threadId,
            query = query,
            role = role?.name,
            part = part,
            generation = generation,
            offset = offset,
            limit = limit,
        )
    }.map { it.toChatDomain() }

    override suspend fun getInvitableMembers(threadId: String, query: String?, offset: Int, limit: Int) =
        apiCall {
            communityThreadApi.getInvitableCommunityThreadMembers(
                threadId = threadId,
                query = query,
                offset = offset,
                limit = limit,
            )
        }.map { it.toChatDomain() }

    override suspend fun inviteMembers(threadId: String, memberIds: List<Long>) =
        apiCall {
            communityThreadApi.inviteCommunityThreadMembers(
                threadId = threadId,
                request = InviteCommunityThreadMembersRequest(memberIds.distinct()),
            )
        }.map { it.toChatDomain() }

    override suspend fun kickMember(threadId: String, memberId: String) =
        apiCall { communityThreadApi.kickCommunityThreadMember(threadId, memberId) }
            .map { it.toChatDomain() }

    override suspend fun leaveThread(threadId: String) =
        apiCall { communityThreadApi.leaveCommunityThread(threadId) }
            .map { it.toChatDomain() }

    override suspend fun changeMemberRole(threadId: String, memberId: String, role: CommunityThreadRole) =
        apiCall { communityThreadApi.changeMemberRole(threadId, memberId, ChangeMemberRoleRequest(role)) }

    override suspend fun reportMessage(messageId: String, reason: CommunityMessageReportReason) =
        apiCall { communityThreadApi.reportMessage(messageId, ReportMessageRequest(reason)) }
}
