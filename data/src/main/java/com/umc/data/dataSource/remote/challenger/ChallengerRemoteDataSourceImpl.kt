package com.umc.data.dataSource.remote.challenger

import com.umc.data.api.ChallengerApi
import com.umc.data.dataSource.base.apiCall
import com.umc.data.response.challenger.ChallengerResponse
import com.umc.data.response.challenger.ChallengerSearchScheduleResponse
import com.umc.data.response.challenger.ChallengerCursorResponse
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.challenger.ChallengerPointRequest
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

    //일정 생성용 유저 검색
    override suspend fun searchChallengerSchedule(
        cursor: Long?,
        size: Int,
        name: String?,
        nickname: String?
    ): ApiState<ChallengerSearchScheduleResponse> {
        return apiCall { challengerApi.searchChallengerSchedule(cursor, size, name, nickname) }
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

}