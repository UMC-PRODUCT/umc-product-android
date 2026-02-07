package com.umc.data.dataSource.remote.attendance

import com.umc.data.response.attendance.AdminPendingUserResponse
import com.umc.data.response.attendance.AttendanceAvailableResponse
import com.umc.domain.model.base.ApiState

interface AttendanceRemoteDataSource {
    suspend fun getAttendanceAvailable(): ApiState<List<AttendanceAvailableResponse>>
    suspend fun postAttendanceCheck(sheetId: Int): ApiState<String>
    suspend fun getPendingUsers(scheduleId: Int): ApiState<List<AdminPendingUserResponse>>
}