package com.umc.data.repository.challenger

import com.umc.data.dataSource.remote.challenger.ChallengerRemoteDataSource
import com.umc.data.request.challenger.AddChallengerPointRequest
import com.umc.data.response.challenger.ChallengerCursorResponse
import com.umc.data.response.challenger.ChallengerResponse.Companion.toManageModel
import com.umc.data.response.challenger.ChallengerResponse.Companion.toModel
import com.umc.data.response.member.MemberResponse.Companion.toDomain
import com.umc.domain.model.UserInfo
import com.umc.domain.model.act.challenger.AdminChallengerList
import com.umc.domain.model.act.challenger.ChallengerInfoDialogModel
import com.umc.domain.model.act.challenger.ChallengerList
import com.umc.domain.model.act.challenger.ChallengerManageDialogModel
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.map
import com.umc.domain.model.home.ParticipantSearchPage
import com.umc.domain.model.request.challenger.ChallengerPointRequest
import com.umc.domain.model.request.challenger.ChallengerRecordMemberRequest
import com.umc.domain.repository.ChallengerRepository
import javax.inject.Inject

class ChallengerRepositoryImpl @Inject constructor(
    private val challengerRemoteDataSource: ChallengerRemoteDataSource,
    private val searchScopeProvider: ChallengerSearchScopeProvider,
) : ChallengerRepository {

    // 챌린저 검색은 모두 여기를 거친다. 중앙운영사무국만 운영진 검색으로 전 학교를 보고,
    // 나머지는 회원 검색에 본인 학교를 넣어 부른다(운영진 검색은 일반 챌린저에게 403).
    private suspend fun searchInScope(
        cursor: Long?,
        size: Int,
        schoolId: Long? = null,
        gisuId: Long? = null,
        part: String? = null,
        keyword: String? = null,
    ): ApiState<ChallengerCursorResponse> {
        val scope = when (val result = searchScopeProvider.current()) {
            is ApiState.Success -> result.data
            is ApiState.Fail -> return result
        }
        return when (scope) {
            ChallengerSearchScope.AllSchools -> challengerRemoteDataSource.getChallengerList(
                cursor = cursor, size = size,
                schoolId = schoolId, gisuId = gisuId,
                part = part, keyword = keyword
            )

            is ChallengerSearchScope.OwnSchool -> challengerRemoteDataSource.searchMembers(
                cursor = cursor, size = size,
                schoolId = scope.schoolId, gisuId = gisuId ?: scope.activeGisuId,
                part = part, keyword = keyword
            )
        }
    }
    override suspend fun getChallengerInfo(id: Long): ApiState<ChallengerInfoDialogModel> {
        return challengerRemoteDataSource.getChallengerDetail(id).map {
            it.toModel()
        }
    }

    override suspend fun getAdminChallengerInfo(id: Long): ApiState<ChallengerManageDialogModel> {
        return challengerRemoteDataSource.getChallengerDetail(id).map {
            it.toManageModel()
        }
    }

    override suspend fun grantChallengerPoint(
        id: Long,
        request: ChallengerPointRequest
    ): ApiState<ChallengerManageDialogModel> {
        return challengerRemoteDataSource.grantChallengerPoint(id, request).map {
            it.toManageModel()
        }
    }

    //일정 생성용 유저 검색

    override suspend fun searchChallengerSchedule(
        cursor: Long?,
        size: Int,
        keyword: String?
    ): ApiState<ParticipantSearchPage> {
        return searchInScope(
            cursor = cursor,
            size = size,
            keyword = keyword
        ).map {
            it.toParticipantSearchPage()
        }
    }

    override suspend fun getChallengerList(
        cursor: Long?, size: Int,
        schoolId: Long?, gisuId: Long?,
        keyword: String?, part: String?
    ): ApiState<ChallengerList> {
        return searchInScope(
            cursor = cursor, size = size,
            schoolId = schoolId, gisuId = gisuId,
            keyword = keyword, part = part
        ).map { it.toModel() }
    }

    override suspend fun getAdminChallengerList(
        cursor: Long?, size: Int,
        schoolId: Long?, gisuId: Long?,
        keyword: String?, part: String?
    ): ApiState<AdminChallengerList> {
        return searchInScope(
            cursor = cursor, size = size,
            schoolId = schoolId, gisuId = gisuId,
            keyword = keyword, part = part
        ).map { it.toAdminList() }
    }

    override suspend fun deleteChallengerPoint(challengerPointId: Long): ApiState<Unit> {
        return challengerRemoteDataSource.deleteChallengerPoint(challengerPointId)
    }

    override suspend fun addChallengerRecordMember(request: ChallengerRecordMemberRequest): ApiState<Unit> {
        return challengerRemoteDataSource.addChallengerRecordMember(request)
    }

    override suspend fun searchChallengerCursor(
        cursor: Long?,
        size: Int,
        schoolId: Long?,
        gisuId: Long?,
        part: String?,
        keyword: String?
    ): ApiState<ChallengerList> {
        return searchInScope(
            cursor = cursor,
            size = size,
            schoolId = schoolId,
            gisuId = gisuId,
            part = part,
            keyword = keyword
        ).map { it.toModel() }
    }

    //챌린저 상벌점 추가
    override suspend fun addChallengerPoint(
        challengerId: Long,
        pointType: String,
        pointValue: Int,
        description: String
    ): ApiState<UserInfo> {
        val request = AddChallengerPointRequest(
            pointType = pointType,
            pointValue = pointValue,
            description = description
        )
        return challengerRemoteDataSource.addChallengerPoint(challengerId, request).map {
            it.toDomain()
        }
    }
}