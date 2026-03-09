package com.umc.data.dataSource.remote.challenger

import com.umc.data.response.challenger.ChallengerCursorResponse
import com.umc.data.response.challenger.ChallengerResponse
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.challenger.ChallengerPointRequest
import com.umc.domain.model.request.challenger.ChallengerRecordMemberRequest

interface ChallengerRemoteDataSource {
    suspend fun getChallengerDetail(id: Long): ApiState<ChallengerResponse>
    suspend fun grantChallengerPoint(id: Long, request: ChallengerPointRequest): ApiState<ChallengerResponse>

    suspend fun getChallengerList(
        cursor: Long?,
        size: Int,
        schoolId: Long? = null,
        gisuId: Long? = null,
        part: String? = null,
        name: String? = null,
        nickname: String? = null,
        keyword: String? = null
    ): ApiState<ChallengerCursorResponse>

    suspend fun deleteChallengerPoint(challengerPointId: Long): ApiState<Unit>

    suspend fun addChallengerRecordMember(request: ChallengerRecordMemberRequest): ApiState<Unit>
}