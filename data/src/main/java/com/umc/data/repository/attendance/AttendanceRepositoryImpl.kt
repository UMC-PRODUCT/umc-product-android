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
import java.time.Instant
import javax.inject.Inject

class AttendanceRepositoryImpl @Inject constructor(
    private val attendanceRemoteDataSource: AttendanceRemoteDataSource
) : AttendanceRepository {

    override suspend fun getAttendanceAvailable(): ApiState<List<UserCheckAvailable>> {
        return attendanceRemoteDataSource.getAttendanceAvailable().map { responseList ->
            val now = Instant.now()
            responseList
                //조회 범위(전날~다음날)에 걸린 이미 끝난 일정은 제외. 승인 대기 세션은 출석 기록에 아직 잡히지 않으므로 유지
                .filterNot { it.isEnded(now) && it.attendanceStatus?.endsWith("_PENDING") != true }
                .map { it.toAvailable() }
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
