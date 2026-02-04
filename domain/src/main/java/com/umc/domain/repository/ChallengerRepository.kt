package com.umc.domain.repository

import com.umc.domain.model.act.challenger.ChallengerInfoDialogModel
import com.umc.domain.model.base.ApiState

interface ChallengerRepository {
    suspend fun getChallengerInfo(id: Long): ApiState<ChallengerInfoDialogModel>
}