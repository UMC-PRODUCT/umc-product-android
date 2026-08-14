package com.umc.data.repository.attendance

import com.umc.data.dataSource.remote.attendance.AttendanceRemoteDataSource
import com.umc.domain.model.act.check.AdminPendingUser
import com.umc.domain.model.act.check.UserCheckAvailable
import com.umc.domain.model.act.check.UserCheckHistory
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.map
import com.umc.domain.model.request.attendance.AttendanceCheckRequest
import com.umc.domain.model.request.attendance.AttendanceReasonRequest
import com.umc.domain.repository.attendance.AttendanceRepository
import javax.inject.Inject

class AttendanceRepositoryImpl @Inject constructor(
    private val attendanceRemoteDataSource: AttendanceRemoteDataSource
) : AttendanceRepository {

    override suspend fun getAttendanceAvailable(): ApiState<List<UserCheckAvailable>> {
        return attendanceRemoteDataSource.getAttendanceAvailable().map { responseList ->
            responseList.map { it.toAvailable() }
        }
    }

    override suspend fun postAttendanceCheck(request: AttendanceCheckRequest): ApiState<String> {
        return attendanceRemoteDataSource.postAttendanceCheck(request)
    }

    override suspend fun getPendingUsers(scheduleId: Long): ApiState<List<AdminPendingUser>> {
        return attendanceRemoteDataSource.getPendingUsers(scheduleId).map { responseList ->
            responseList.pendingUsers()
        }
    }

    override suspend fun decideAttendance(
        scheduleId: Long,
        memberIds: List<Long>,
        approved: Boolean
    ): ApiState<Unit> = attendanceRemoteDataSource.decideAttendance(scheduleId, memberIds, approved)

    override suspend fun postAttendanceReason(request: AttendanceReasonRequest): ApiState<String> {
        return attendanceRemoteDataSource.postAttendanceReason(request)
    }

    override suspend fun getAttendanceHistory(): ApiState<List<UserCheckHistory>> {
        return attendanceRemoteDataSource.getAttendanceHistory().map { responseList ->
            responseList
                .filter { it.attendanceStatus in completedAttendanceStatuses }
                .mapIndexed { index, response -> response.toHistory(index) }
        }
    }

    private companion object {
        val completedAttendanceStatuses = setOf("PRESENT", "LATE", "EXCUSED", "ABSENT")
    }
}
