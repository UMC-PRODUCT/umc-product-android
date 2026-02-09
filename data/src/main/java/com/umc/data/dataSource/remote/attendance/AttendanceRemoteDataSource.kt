package com.umc.data.dataSource.remote.attendance

import com.umc.data.response.attendance.AdminPendingUserResponse
import com.umc.data.response.attendance.AttendanceAvailableResponse
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.attendance.AttendanceCheckRequest

interface AttendanceRemoteDataSource {
    suspend fun getAttendanceAvailable(): ApiState<List<AttendanceAvailableResponse>>
    suspend fun postAttendanceCheck(request: AttendanceCheckRequest): ApiState<String>
    suspend fun getPendingUsers(scheduleId: Int): ApiState<List<AdminPendingUserResponse>>
    suspend fun approveAttendance(recordId: Int): ApiState<Unit>
    suspend fun rejectAttendance(recordId: Int): ApiState<Unit>
}