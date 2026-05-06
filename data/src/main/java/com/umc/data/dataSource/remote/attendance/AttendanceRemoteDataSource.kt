package com.umc.data.dataSource.remote.attendance

import com.umc.data.response.attendance.AdminPendingUserResponse
import com.umc.data.response.attendance.ChallengerAttendanceHistoryResponse
import com.umc.domain.model.base.ApiState

interface AttendanceRemoteDataSource {
    suspend fun getPendingUsers(scheduleId: Long): ApiState<List<AdminPendingUserResponse>>
    suspend fun getChallengerAttendanceHistory(challengerId: Long): ApiState<List<ChallengerAttendanceHistoryResponse>>
}