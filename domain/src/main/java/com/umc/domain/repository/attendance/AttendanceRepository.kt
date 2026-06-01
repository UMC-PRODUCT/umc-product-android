package com.umc.domain.repository.attendance

import com.umc.domain.model.act.challenger.ChallengerInfoHistory
import com.umc.domain.model.base.ApiState

interface AttendanceRepository {
    suspend fun getChallengerAttendanceHistory(challengerId: Long): ApiState<List<ChallengerInfoHistory>>
}