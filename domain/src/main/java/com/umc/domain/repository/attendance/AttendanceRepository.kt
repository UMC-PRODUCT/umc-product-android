package com.umc.domain.repository.attendance

import com.umc.domain.model.act.check.AdminPendingUser
import com.umc.domain.model.act.check.UserCheckAvailable
import com.umc.domain.model.base.ApiState

interface AttendanceRepository {
    suspend fun getAttendanceAvailable(): ApiState<List<UserCheckAvailable>>
    suspend fun postAttendanceCheck(sheetId: Int): ApiState<String>
    suspend fun getPendingUsers(scheduleId: Int): ApiState<List<AdminPendingUser>>
}