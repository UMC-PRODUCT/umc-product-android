package com.umc.data.dataSource.remote.attendance

import com.umc.data.response.attendance.AdminPendingUserResponse
import com.umc.data.response.attendance.AttendanceAvailableResponse
import com.umc.data.response.attendance.ChallengerAttendanceHistoryResponse
import com.umc.data.response.attendance.UserCheckHistoryResponse
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.attendance.AttendanceReasonRequest

interface AttendanceRemoteDataSource {
    suspend fun getAttendanceAvailable(): ApiState<List<AttendanceAvailableResponse>>
    suspend fun getPendingUsers(scheduleId: Long): ApiState<List<AdminPendingUserResponse>>
    suspend fun postAttendanceReason(request: AttendanceReasonRequest): ApiState<String>
    suspend fun getAttendanceHistory(): ApiState<List<UserCheckHistoryResponse>>
    suspend fun getChallengerAttendanceHistory(challengerId: Long): ApiState<List<ChallengerAttendanceHistoryResponse>>
}