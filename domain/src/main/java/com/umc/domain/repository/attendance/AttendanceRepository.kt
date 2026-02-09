package com.umc.domain.repository.attendance

import com.umc.domain.model.act.check.AdminPendingUser
import com.umc.domain.model.act.check.UserCheckAvailable
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.attendance.AttendanceCheckRequest

interface AttendanceRepository {
    suspend fun getAttendanceAvailable(): ApiState<List<UserCheckAvailable>>
    suspend fun postAttendanceCheck(request: AttendanceCheckRequest): ApiState<String>
    suspend fun getPendingUsers(scheduleId: Int): ApiState<List<AdminPendingUser>>
    suspend fun approveAttendance(recordId: Int): ApiState<Unit>
    suspend fun rejectAttendance(recordId: Int): ApiState<Unit>
}