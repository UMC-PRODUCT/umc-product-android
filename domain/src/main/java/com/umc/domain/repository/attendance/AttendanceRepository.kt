package com.umc.domain.repository.attendance

import com.umc.domain.model.act.check.UserCheckAvailable
import com.umc.domain.model.base.ApiState

interface AttendanceRepository {
    suspend fun getAttendanceAvailable(): ApiState<List<UserCheckAvailable>>
}