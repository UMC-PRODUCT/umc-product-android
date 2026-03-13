package com.umc.data.repository.challenger

import com.umc.data.dataSource.remote.challenger.ChallengerRemoteDataSource
import com.umc.data.response.challenger.ChallengerResponse.Companion.toManageModel
import com.umc.data.response.challenger.ChallengerResponse.Companion.toModel
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
    private val challengerRemoteDataSource: ChallengerRemoteDataSource
) : ChallengerRepository {
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
        return challengerRemoteDataSource.getChallengerList(
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
        return challengerRemoteDataSource.getChallengerList(
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
        return challengerRemoteDataSource.getChallengerList(
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
        return challengerRemoteDataSource.getChallengerList(
            cursor = cursor,
            size = size,
            schoolId = schoolId,
            gisuId = gisuId,
            part = part,
            keyword = keyword
        ).map { it.toModel() }
    }
}