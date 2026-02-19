package com.umc.data.dataSource.remote.challenger

import com.umc.data.api.ChallengerApi
import com.umc.data.dataSource.base.apiCall
import com.umc.data.response.challenger.ChallengerResponse
import com.umc.data.response.challenger.ChallengerCursorResponse
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.challenger.ChallengerPointRequest
import com.umc.domain.model.request.challenger.ChallengerRecordMemberRequest
import javax.inject.Inject

class ChallengerRemoteDataSourceImpl @Inject constructor(
    private val challengerApi: ChallengerApi
) : ChallengerRemoteDataSource {

    override suspend fun getChallengerDetail(id: Long): ApiState<ChallengerResponse> {
        return apiCall { challengerApi.getChallengerDetail(id) }
    }

    override suspend fun grantChallengerPoint(id: Long, request: ChallengerPointRequest): ApiState<ChallengerResponse> {
        return apiCall { challengerApi.grantChallengerPoint(id, request) }
    }

    override suspend fun getChallengerList(
        cursor: Long?,
        size: Int,
        schoolId: Long?,
        gisuId: Long?,
        part: String?,
        name: String?,
        nickname: String?
    ): ApiState<ChallengerCursorResponse> {
        return apiCall {
            challengerApi.getChallengers(cursor, size, part, name, nickname, schoolId, null, gisuId)
        }
    }

    override suspend fun deleteChallengerPoint(challengerPointId: Long): ApiState<Unit> {
        return apiCall { challengerApi.deleteChallengerPoint(challengerPointId) }
    }

    override suspend fun addChallengerRecordMember(request: ChallengerRecordMemberRequest): ApiState<Unit> {
        return apiCall { challengerApi.addChallengerRecordMember(request) }
    }
}