package com.umc.domain.repository

import com.umc.domain.model.act.challenger.ChallengerInfoDialogModel
import com.umc.domain.model.act.challenger.ChallengerManageDialogModel
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.home.ParticipantSearchPage
import com.umc.domain.model.request.challenger.ChallengerPointRequest

interface ChallengerRepository {
    // 유저용 상세 정보
    suspend fun getChallengerInfo(id: Long): ApiState<ChallengerInfoDialogModel>

    // 관리자용 상세 정보
    suspend fun getAdminChallengerInfo(id: Long): ApiState<ChallengerManageDialogModel>

    // 챌린저 상벌점 부여
    suspend fun grantChallengerPoint(id: Long, request: ChallengerPointRequest): ApiState<ChallengerManageDialogModel>

    // 일정 생성용 유저 검색하기
    suspend fun searchChallengerSchedule(cursor: Long?, size: Int, name: String?, nickname: String?): ApiState<ParticipantSearchPage>
}