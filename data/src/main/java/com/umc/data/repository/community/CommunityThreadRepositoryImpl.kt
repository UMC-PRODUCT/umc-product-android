package com.umc.data.repository.community

import com.google.gson.Gson
import com.google.gson.stream.MalformedJsonException
import com.umc.domain.model.base.ApiResponse
import retrofit2.HttpException
import com.umc.data.api.ChangeMemberRoleRequest
import com.umc.data.api.CommunityThreadApi
import com.umc.data.api.ReportMessageRequest
import com.umc.data.dataSource.base.NETWORK_ERROR_MESSAGE
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
import java.io.EOFException
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class CommunityThreadRepositoryImpl @Inject constructor(
    private val communityThreadApi: CommunityThreadApi,
) : CommunityThreadRepository {

    override suspend fun getCommunityThreads(
        filter: String,
        query: String?,
        offset: Int,
        limit: Int,
    ): Result<CommunityThreadPage> {
        return communityCall(fallback = "스레드 목록을 불러오지 못했어요.") {
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
        return communityCall(fallback = "스레드 정보를 불러오지 못했어요.") {
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
        return communityCall(fallback = "멤버 목록을 불러오지 못했어요.") {
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
        return communityCall(fallback = "멤버를 초대하지 못했어요.") {
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
        return communityCall(fallback = "스레드를 만들지 못했어요.") {
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
        return communityCall(fallback = "스레드를 수정하지 못했어요.") {

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
        return communityCall(fallback = "스레드에서 나가지 못했어요.") {
            val response =
                communityThreadApi.leaveCommunityThread(
                    threadId = threadId,
                )

            requireNotNull(response.result) {
                response.message.ifBlank {
                    "스레드 나가기 응답 데이터가 없어요."
                }
            }

            Unit
        }
    }

    override suspend fun deleteCommunityThread(
        threadId: String,
    ): Result<CommunityThreadDetail> {
        return communityCall(fallback = "스레드를 삭제하지 못했어요.") {
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
        return communityCall(fallback = "알림 설정을 변경하지 못했어요.") {
            val response =
                communityThreadApi.muteCommunityThread(
                    threadId = threadId
                )

            requireNotNull(response.result) {
                response.message.ifBlank {
                    "알림 설정 응답 데이터가 없어요."
                }
            }.toDomain()
        }
    }


    override suspend fun unmuteCommunityThread(
        threadId: String,
    ): Result<CommunityThreadDetail> {
        return communityCall(fallback = "알림 설정을 변경하지 못했어요.") {
            val response =
                communityThreadApi.unmuteCommunityThread(
                    threadId = threadId
                )

            requireNotNull(response.result) {
                response.message.ifBlank {
                    "알림 설정 응답 데이터가 없어요."
                }
            }.toDomain()
        }
    }


    override suspend fun pinCommunityThread(
        threadId: String,
    ): Result<CommunityThreadDetail> {
        return communityCall(fallback = "스레드 고정 상태를 변경하지 못했어요.") {

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
        return communityCall(fallback = "스레드 고정 상태를 변경하지 못했어요.") {

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
        return communityCall(fallback = "멤버를 삭제하지 못했어요.") {
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
        return communityCall(fallback = "멤버 목록을 불러오지 못했어요.") {
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


    /**
     * 커뮤니티 스레드 API 호출을 감싸 사용자에게 그대로 보여줘도 되는 한글 메시지의 실패로 바꾼다.
     * ViewModel 이 `throwable.message` 를 토스트에 그대로 쓰므로 OkHttp/JVM 영문 원문이 새지 않게 한다.
     */
    private suspend inline fun <T> communityCall(
        fallback: String,
        block: () -> T,
    ): Result<T> {
        return try {
            Result.success(block())
        } catch (e: Throwable) {
            Result.failure(e.toUserFacingException(fallback))
        }
    }

    private fun Throwable.toUserFacingException(fallback: String): Exception {
        return when (this) {
            is CancellationException -> throw this
            is HttpException -> Exception(getServerMessage(fallback))
            // 응답 본문 변환 실패는 연결 문제가 아니므로 네트워크 문구를 쓰지 않는다.
            is MalformedJsonException, is EOFException -> Exception(fallback)
            is IOException -> Exception(NETWORK_ERROR_MESSAGE)
            // 서버가 내려준 한글 메시지(requireNotNull 등)만 살리고 영문 원문은 fallback 으로 대체한다.
            else -> Exception(
                message?.takeIf { msg -> msg.any { it in '가'..'힣' } } ?: fallback
            )
        }
    }

    private fun HttpException.getServerMessage(
        fallback: String,
    ): String {
        return try {
            val errorBody =
                response()?.errorBody()?.string()

            if (errorBody.isNullOrBlank()) {
                return fallback
            }

            val errorResponse =
                Gson().fromJson(
                    errorBody,
                    ApiResponse::class.java,
                )

            errorResponse.message
                ?.takeIf { it.isNotBlank() }
                ?: fallback
        } catch (e: Exception) {
            fallback
        }
    }
}
