package com.umc.domain.repository

import com.umc.domain.model.act.challenger.ChallengerInfoDialogModel
import com.umc.domain.model.act.challenger.ChallengerManageDialogModel
import com.umc.domain.model.base.ApiState

interface ChallengerRepository {
    // 유저용 상세 정보
    suspend fun getChallengerInfo(id: Long): ApiState<ChallengerInfoDialogModel>

    // 관리자용 상세 정보
    suspend fun getAdminChallengerInfo(id: Long): ApiState<ChallengerManageDialogModel>
}