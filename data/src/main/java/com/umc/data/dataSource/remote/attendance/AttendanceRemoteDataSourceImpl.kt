package com.umc.data.dataSource.remote.attendance

import com.umc.data.api.AttendanceApi
import com.umc.data.dataSource.base.apiCall
import com.umc.data.response.attendance.AttendanceAvailableResponse
import com.umc.domain.model.base.ApiState
import javax.inject.Inject

class AttendanceRemoteDataSourceImpl @Inject constructor(
    private val attendanceApi: AttendanceApi
) : AttendanceRemoteDataSource {

    override suspend fun getAttendanceAvailable(): ApiState<List<AttendanceAvailableResponse>> {
        return apiCall { attendanceApi.getAttendanceAvailable() }
    }
}