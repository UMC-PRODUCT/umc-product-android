package com.umc.data.repository.community

import android.os.SystemClock
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
import com.umc.data.dataSource.remote.challenger.ChallengerRemoteDataSource
import com.umc.data.repository.challenger.ChallengerSearchScope
import com.umc.data.repository.challenger.ChallengerSearchScopeProvider
import com.umc.data.response.community.CommunityThreadInvitablePageResponse
import com.umc.data.response.community.CommunityThreadInvitableResponse
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.FailState
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
    private val challengerRemoteDataSource: ChallengerRemoteDataSource,
    private val searchScopeProvider: ChallengerSearchScopeProvider,
) : CommunityThreadRepository {

    /**
     * 초대 후보 API 에는 학교 조건이 없어 전 학교 회원이 나온다.
     * 중앙운영사무국이 아니면 본인 학교 회원 검색 결과와 겹치는 사람만 남긴다.
     * 이미 참여 중·강퇴·차단 같은 제외 조건은 서버 초대 후보 결과를 그대로 따른다.
     *
     * 걸러서 비어 버린 페이지 때문에 다음 페이지를 못 부르는 일이 없게, limit 만큼 모이거나 끝날 때까지 서버 페이지를 이어 받는다.
     */
    private suspend fun getInvitableInScope(
        threadId: String,
        query: String?,
        offset: Int,
        limit: Int,
    ): ApiResponse<CommunityThreadInvitablePageResponse> {
        val schoolId = when (val scope = searchScopeProvider.current()) {
            is ApiState.Fail -> return scope.failState.toFailResponse()
            is ApiState.Success -> (scope.data as? ChallengerSearchScope.OwnSchool)?.schoolId
        } ?: return communityThreadApi.getInvitableCommunityThreadMembers(
            threadId = threadId,
            query = query,
            offset = offset,
            limit = limit,
        )

        val schoolMemberIds = when (val ids = loadSchoolMemberIds(schoolId, query)) {
            is ApiState.Fail -> return ids.failState.toFailResponse()
            is ApiState.Success -> ids.data
        }
        // 검색어에 맞는 본인 학교 회원이 없으면 초대 후보를 훑지 않고 끝낸다
        if (schoolMemberIds.isEmpty()) {
            return ApiResponse(
                success = true,
                code = "",
                message = "",
                result = CommunityThreadInvitablePageResponse(),
            )
        }

        val items = mutableListOf<CommunityThreadInvitableResponse>()
        var nextOffset: Int? = offset
        var lastResponse: ApiResponse<CommunityThreadInvitablePageResponse>? = null
        while (nextOffset != null && items.size < limit) {
            val response = communityThreadApi.getInvitableCommunityThreadMembers(
                threadId = threadId,
                query = query,
                offset = nextOffset,
                limit = INVITABLE_SCAN_LIMIT,
            )
            val page = response.result
            if (!response.success || page == null) return response
            items += page.items.filter { it.memberId.toLongOrNull() in schoolMemberIds }
            nextOffset = page.nextOffset?.toIntOrNull()
            lastResponse = response
        }

        return ApiResponse(
            success = true,
            code = lastResponse?.code.orEmpty(),
            message = lastResponse?.message.orEmpty(),
            result = CommunityThreadInvitablePageResponse(
                items = items,
                nextOffset = nextOffset?.toString(),
                total = items.size.toString(),
            ),
        )
    }

    // 다음 페이지를 부를 때 같은 학교 회원 id 를 다시 받지 않도록 마지막 결과를 잠깐 보관한다
    @Volatile
    private var schoolMemberIdsCache: SchoolMemberIdsCache? = null

    private class SchoolMemberIdsCache(
        val schoolId: Long,
        val query: String?,
        val ids: Set<Long>,
        val savedAt: Long,
    )

    // 본인 학교 회원 id. 초대 후보와 같은 검색어로 좁혀 받고, 초대는 기수와 무관하므로 기수는 따지지 않는다
    private suspend fun loadSchoolMemberIds(
        schoolId: Long,
        query: String?,
    ): ApiState<Set<Long>> {
        val keyword = query?.takeIf { it.isNotBlank() }
        schoolMemberIdsCache
            ?.takeIf { it.schoolId == schoolId && it.query == keyword }
            ?.takeIf { SystemClock.elapsedRealtime() - it.savedAt < SCHOOL_MEMBER_CACHE_MILLIS }
            ?.let { return ApiState.Success(it.ids) }

        val ids = mutableSetOf<Long>()
        var cursor: Long? = null
        for (pageIndex in 0 until MAX_SCHOOL_MEMBER_PAGES) {
            val page = challengerRemoteDataSource.searchMembers(
                cursor = cursor,
                size = SCHOOL_MEMBER_PAGE_SIZE,
                schoolId = schoolId,
                keyword = keyword,
            )
            when (page) {
                is ApiState.Fail -> return page
                is ApiState.Success -> {
                    page.data.cursor.content.mapTo(ids) { it.memberId }
                    cursor = page.data.cursor.nextCursor ?: break
                }
            }
        }
        schoolMemberIdsCache = SchoolMemberIdsCache(schoolId, keyword, ids, SystemClock.elapsedRealtime())
        return ApiState.Success(ids)
    }

    private fun FailState.toFailResponse(): ApiResponse<CommunityThreadInvitablePageResponse> =
        ApiResponse(success = false, code = code, message = message)

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
                getInvitableInScope(
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
            .map { response ->
                // 나간 스레드는 서버가 isJoined=false 와 함께 이전 myRole 을 그대로 내려준다.
                // 서버가 명시적으로 참여 중이 아니라고 한 경우에만 역할 없음으로 맞춰 보기 전용으로 연다.
                val detail = response.toChatDomain()
                if (response.isJoined == false) detail.copy(myRole = CommunityThreadRole.UNKNOWN) else detail
            }

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
            getInvitableInScope(
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

    private companion object {
        // 초대 후보 API 의 limit 상한
        const val INVITABLE_SCAN_LIMIT = 100
        const val SCHOOL_MEMBER_PAGE_SIZE = 100
        const val MAX_SCHOOL_MEMBER_PAGES = 20
        const val SCHOOL_MEMBER_CACHE_MILLIS = 30_000L
    }
}
