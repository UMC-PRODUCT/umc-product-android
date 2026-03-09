package com.umc.domain.repository

import com.umc.domain.model.act.challenger.AdminChallengerList
import com.umc.domain.model.act.challenger.ChallengerInfoDialogModel
import com.umc.domain.model.act.challenger.ChallengerList
import com.umc.domain.model.act.challenger.ChallengerManageDialogModel
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.home.ParticipantSearchPage
import com.umc.domain.model.request.challenger.ChallengerPointRequest
import com.umc.domain.model.request.challenger.ChallengerRecordMemberRequest

interface ChallengerRepository {
    // 유저용 상세 정보
    suspend fun getChallengerInfo(id: Long): ApiState<ChallengerInfoDialogModel>

    // 관리자용 상세 정보
    suspend fun getAdminChallengerInfo(id: Long): ApiState<ChallengerManageDialogModel>

    // 챌린저 상벌점 부여
    suspend fun grantChallengerPoint(id: Long, request: ChallengerPointRequest): ApiState<ChallengerManageDialogModel>

    // 일정 생성용 유저 검색하기
    suspend fun searchChallengerSchedule(cursor: Long?, size: Int, keyword: String?): ApiState<ParticipantSearchPage>

    suspend fun getChallengerList(
        cursor: Long?,
        size: Int,
        schoolId: Long?,
        gisuId: Long?
    ): ApiState<ChallengerList>

    suspend fun getAdminChallengerList(
        cursor: Long?,
        size: Int,
        schoolId: Long?,
        gisuId: Long?
    ): ApiState<AdminChallengerList>

    suspend fun deleteChallengerPoint(challengerPointId: Long): ApiState<Unit>

    // 챌린저 기록 멤버 추가 (코드로 참여)
    suspend fun addChallengerRecordMember(request: ChallengerRecordMemberRequest): ApiState<Unit>


    suspend fun searchChallengerCursor(
        cursor: Long?,
        size: Int,
        schoolId: Long?,
        gisuId: Long?,
        part: String?,
        name: String?,
        nickname: String?
    ): ApiState<ChallengerList>

}