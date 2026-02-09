package com.umc.data.dataSource.remote.attendance

import com.umc.data.api.AttendanceApi
import com.umc.data.dataSource.base.apiCall
import com.umc.data.response.attendance.AdminPendingUserResponse
import com.umc.data.response.attendance.AttendanceAvailableResponse
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.attendance.AttendanceCheckRequest
import javax.inject.Inject

class AttendanceRemoteDataSourceImpl @Inject constructor(
    private val attendanceApi: AttendanceApi
) : AttendanceRemoteDataSource {

    override suspend fun getAttendanceAvailable(): ApiState<List<AttendanceAvailableResponse>> {
        return apiCall { attendanceApi.getAttendanceAvailable() }
    }

    override suspend fun postAttendanceCheck(request: AttendanceCheckRequest): ApiState<String> {
        return apiCall { attendanceApi.postAttendanceCheck(request) }
    }

    override suspend fun getPendingUsers(scheduleId: Int): ApiState<List<AdminPendingUserResponse>> {
        return apiCall { attendanceApi.getPendingUsers(scheduleId) }
    }

    override suspend fun approveAttendance(recordId: Int): ApiState<Unit> {
        return apiCall { attendanceApi.approveAttendance(recordId) }
    }

    override suspend fun rejectAttendance(recordId: Int): ApiState<Unit> {
        return apiCall { attendanceApi.rejectAttendance(recordId) }
    }

}