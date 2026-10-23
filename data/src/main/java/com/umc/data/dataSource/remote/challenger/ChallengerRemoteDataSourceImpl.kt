package com.umc.data.dataSource.remote.challenger

import com.umc.data.api.ChallengerApi
import com.umc.data.dataSource.base.apiCall
import com.umc.data.request.challenger.AddChallengerPointRequest
import com.umc.data.response.challenger.ChallengerResponse
import com.umc.data.response.challenger.ChallengerCursorResponse
import com.umc.data.response.member.MemberResponse
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.mapCatching
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
        nickname: String?,
        keyword: String?
    ): ApiState<ChallengerCursorResponse> {
        // 변환은 apiCall 밖에서 일어나 예외가 잡히지 않는다. 응답이 약속과 달라도 앱이 종료되지 않게 실패로 바꾼다.
        return apiCall {
            challengerApi.getChallengers(
                page = cursor?.toInt() ?: 0,
                size = size,
                part = part,
                keyword = keyword,
                schoolId = schoolId,
                chapterId = null,
                gisuId = gisuId
            )
        }.mapCatching { it.toCursorResponse() }
    }

    override suspend fun deleteChallengerPoint(challengerPointId: Long): ApiState<Unit> {
        return apiCall { challengerApi.deleteChallengerPoint(challengerPointId) }
    }

    override suspend fun addChallengerRecordMember(request: ChallengerRecordMemberRequest): ApiState<Unit> {
        return apiCall { challengerApi.addChallengerRecordMember(request) }
    }

    override suspend fun addChallengerPoint(
        challengerId: Long,
        request: AddChallengerPointRequest
    ): ApiState<MemberResponse> {
        return apiCall { challengerApi.addChallengerPoint(challengerId, request) }
    }
}
