package com.umc.domain.repository.schedule

import com.umc.domain.model.act.check.UserCheckAvailable
import com.umc.domain.model.base.ApiState

interface ScheduleRepository {
    suspend fun getScheduleDetail(scheduleId: Int): ApiState<UserCheckAvailable>
}