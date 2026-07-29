package com.umc.data.dataSource.remote.attendance

import com.umc.data.api.AttendanceApi
import com.umc.data.dataSource.base.apiCall
import com.umc.data.response.attendance.AdminScheduleAttendanceV2Response
import com.umc.data.response.attendance.AttendanceCheckV2Request
import com.umc.data.response.attendance.AttendanceDecisionV2Request
import com.umc.data.response.attendance.AttendanceExcuseV2Request
import com.umc.data.response.attendance.ScheduleAttendanceV2Response
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.map
import com.umc.domain.model.request.attendance.AttendanceCheckRequest
import com.umc.domain.model.request.attendance.AttendanceReasonRequest
import javax.inject.Inject
import java.time.Instant
import java.time.temporal.ChronoUnit

class AttendanceRemoteDataSourceImpl @Inject constructor(
    private val attendanceApi: AttendanceApi
) : AttendanceRemoteDataSource {

    override suspend fun getAttendanceAvailable(): ApiState<List<ScheduleAttendanceV2Response>> {
        val now = Instant.now()
        return apiCall {
            attendanceApi.getAttendanceAvailable(
                from = now.minus(1, ChronoUnit.DAYS).toString(),
                to = now.plus(1, ChronoUnit.DAYS).toString()
            )
        }
    }

    override suspend fun postAttendanceCheck(request: AttendanceCheckRequest): ApiState<String> {
        return apiCall {
            attendanceApi.postAttendanceCheck(
                scheduleId = request.attendanceSheetId,
                request = AttendanceCheckV2Request(
                    locationVerified = request.locationVerified,
                    latitude = request.latitude,
                    longitude = request.longitude
                )
            )
        }.map { "" }
    }

    override suspend fun getPendingUsers(scheduleId: Long): ApiState<AdminScheduleAttendanceV2Response> {
        return apiCall { attendanceApi.getPendingUsers(scheduleId) }
    }

    override suspend fun decideAttendance(
        scheduleId: Long,
        memberIds: List<Long>,
        approved: Boolean
    ): ApiState<Unit> {
        return apiCall {
            attendanceApi.decideAttendance(
                scheduleId,
                memberIds.map { AttendanceDecisionV2Request(it, approved) }
            )
        }.map { Unit }
    }

    override suspend fun postAttendanceReason(request: AttendanceReasonRequest): ApiState<String> {
        return apiCall {
            attendanceApi.postAttendanceReason(
                scheduleId = request.attendanceSheetId,
                request = AttendanceExcuseV2Request(excuseReason = request.reason)
            )
        }.map { "" }
    }

    override suspend fun getAttendanceHistory(): ApiState<List<ScheduleAttendanceV2Response>> {
        val now = Instant.now()
        return apiCall {
            attendanceApi.getAttendanceHistory(
                from = now.minus(3650, ChronoUnit.DAYS).toString(),
                to = now.toString()
            )
        }
    }

}
