package com.umc.data.repository.challenger

import com.umc.data.dataSource.remote.challenger.ChallengerRemoteDataSource
import com.umc.data.response.challenger.ChallengerResponse.Companion.toManageModel
import com.umc.data.response.challenger.ChallengerResponse.Companion.toModel
import com.umc.data.response.challenger.ChallengerSearchScheduleResponse.Companion.toParticipantSearchPage
import com.umc.domain.model.act.challenger.AdminChallengerList
import com.umc.domain.model.act.challenger.ChallengerInfoDialogModel
import com.umc.domain.model.act.challenger.ChallengerList
import com.umc.domain.model.act.challenger.ChallengerManageDialogModel
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.map
import com.umc.domain.model.home.ParticipantSearchPage
import com.umc.domain.model.request.challenger.ChallengerPointRequest
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
        name: String?,
        nickname: String?
    ): ApiState<ParticipantSearchPage> {
        return challengerRemoteDataSource.searchChallengerSchedule(cursor, size, name, nickname).map {
            it.toParticipantSearchPage()
        }
    }

    override suspend fun getChallengerList(
        cursor: Long?,
        size: Int,
        schoolId: Long?,
        gisuId: Long?
    ): ApiState<ChallengerList> {
        return challengerRemoteDataSource.getChallengerList(cursor, size, schoolId, gisuId).map {
            it.toModel()
        }
    }

    override suspend fun getAdminChallengerList(
        cursor: Long?,
        size: Int,
        schoolId: Long?,
        gisuId: Long?
    ): ApiState<AdminChallengerList> {
        return challengerRemoteDataSource.getChallengerList(cursor, size, schoolId, gisuId)
            .map { response ->
                response.toAdminList()
            }
    }
}