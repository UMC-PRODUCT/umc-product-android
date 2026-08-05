package com.umc.data.dataSource.remote.attendance

import com.umc.data.response.attendance.AdminScheduleAttendanceV2Response
import com.umc.data.response.attendance.ScheduleAttendanceV2Response
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.attendance.AttendanceCheckRequest
import com.umc.domain.model.request.attendance.AttendanceReasonRequest

interface AttendanceRemoteDataSource {
    suspend fun getAttendanceAvailable(): ApiState<List<ScheduleAttendanceV2Response>>
    suspend fun postAttendanceCheck(request: AttendanceCheckRequest): ApiState<String>
    suspend fun getPendingUsers(scheduleId: Long): ApiState<AdminScheduleAttendanceV2Response>
    suspend fun decideAttendance(scheduleId: Long, memberIds: List<Long>, approved: Boolean): ApiState<Unit>
    suspend fun postAttendanceReason(request: AttendanceReasonRequest): ApiState<String>
    suspend fun getAttendanceHistory(): ApiState<List<ScheduleAttendanceV2Response>>
}
