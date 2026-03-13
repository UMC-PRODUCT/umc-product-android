package com.umc.data.dataSource.remote.attendance

import com.umc.data.api.AttendanceApi
import com.umc.data.dataSource.base.apiCall
import com.umc.data.response.attendance.AdminPendingUserResponse
import com.umc.data.response.attendance.AttendanceAvailableResponse
import com.umc.data.response.attendance.ChallengerAttendanceHistoryResponse
import com.umc.data.response.attendance.UserCheckHistoryResponse
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.attendance.AttendanceCheckRequest
import com.umc.domain.model.request.attendance.AttendanceReasonRequest
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

    override suspend fun getPendingUsers(scheduleId: Long): ApiState<List<AdminPendingUserResponse>> {
        return apiCall { attendanceApi.getPendingUsers(scheduleId) }
    }

    override suspend fun approveAttendance(recordId: Long): ApiState<Unit> {
        return apiCall { attendanceApi.approveAttendance(recordId) }
    }

    override suspend fun rejectAttendance(recordId: Long): ApiState<Unit> {
        return apiCall { attendanceApi.rejectAttendance(recordId) }
    }

    override suspend fun postAttendanceReason(request: AttendanceReasonRequest): ApiState<String> {
        return apiCall { attendanceApi.postAttendanceReason(request) }
    }

    override suspend fun getAttendanceHistory(): ApiState<List<UserCheckHistoryResponse>> {
        return apiCall { attendanceApi.getAttendanceHistory() }
    }

    override suspend fun getChallengerAttendanceHistory(challengerId: Long): ApiState<List<ChallengerAttendanceHistoryResponse>> {
        return apiCall { attendanceApi.getChallengerAttendanceHistory(challengerId) }
    }

}