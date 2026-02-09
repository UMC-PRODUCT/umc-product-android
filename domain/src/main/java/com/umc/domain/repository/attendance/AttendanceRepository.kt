package com.umc.domain.repository.attendance

import com.umc.domain.model.act.check.AdminPendingUser
import com.umc.domain.model.act.check.UserCheckAvailable
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.attendance.AttendanceCheckRequest
import com.umc.domain.model.request.attendance.AttendanceReasonRequest

interface AttendanceRepository {
    suspend fun getAttendanceAvailable(): ApiState<List<UserCheckAvailable>>
    suspend fun postAttendanceCheck(request: AttendanceCheckRequest): ApiState<String>
    suspend fun getPendingUsers(scheduleId: Long): ApiState<List<AdminPendingUser>>
    suspend fun approveAttendance(recordId: Long): ApiState<Unit>
    suspend fun rejectAttendance(recordId: Long): ApiState<Unit>
    suspend fun postAttendanceReason(request: AttendanceReasonRequest): ApiState<String>
}