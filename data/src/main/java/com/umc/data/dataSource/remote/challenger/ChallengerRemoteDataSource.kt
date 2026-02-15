package com.umc.data.dataSource.remote.challenger

import com.umc.data.response.challenger.ChallengerCursorResponse
import com.umc.data.response.challenger.ChallengerResponse
import com.umc.data.response.challenger.ChallengerSearchScheduleResponse
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.challenger.ChallengerPointRequest

interface ChallengerRemoteDataSource {
    suspend fun getChallengerDetail(id: Long): ApiState<ChallengerResponse>
    suspend fun grantChallengerPoint(id: Long, request: ChallengerPointRequest): ApiState<ChallengerResponse>

    //일정 생성용 챌린저 검색
    suspend fun searchChallengerSchedule(cursor: Long?, size: Int, name: String?, nickname: String?):
            ApiState<ChallengerSearchScheduleResponse>

    suspend fun getChallengerList(
        cursor: Long?,
        size: Int,
        schoolId: Long?,
        gisuId: Long?,
        part: String? = null,
        name: String? = null,
        nickname: String? = null
    ): ApiState<ChallengerCursorResponse>
}