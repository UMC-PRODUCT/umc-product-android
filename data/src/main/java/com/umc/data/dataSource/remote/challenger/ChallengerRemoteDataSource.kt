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
        keyword: String? = null,
    ): ApiState<ChallengerCursorResponse>

    /** 운영진 목록 전용 조회. 선택 기수와 조직 조건의 권한은 서버의 v2 검색에서 검사한다. */
    suspend fun getAdminChallengerList(
        cursor: Long?,
        size: Int,
        schoolId: Long?,
        chapterId: Long?,
        gisuId: Long,
        part: String?,
        keyword: String?,
    ): ApiState<ChallengerCursorResponse>

    // 학교를 반드시 지정하는 회원 검색. 운영진 전용 검색을 쓸 수 없는 사용자에게 쓴다
    suspend fun searchMembers(
        cursor: Long?,
        size: Int,
        schoolId: Long,
        gisuId: Long? = null,
        part: String? = null,
        keyword: String? = null
    ): ApiState<ChallengerCursorResponse>

    suspend fun deleteChallengerPoint(challengerPointId: Long): ApiState<Unit>

    suspend fun addChallengerRecordMember(request: ChallengerRecordMemberRequest): ApiState<Unit>


}
