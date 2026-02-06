package com.umc.data.dataSource.remote.attendance

import com.umc.data.response.attendance.AttendanceAvailableResponse
import com.umc.domain.model.base.ApiState

interface AttendanceRemoteDataSource {
    suspend fun getAttendanceAvailable(): ApiState<List<AttendanceAvailableResponse>>
}