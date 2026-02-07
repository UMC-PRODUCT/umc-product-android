package com.umc.data.repository.attendance

import com.umc.data.dataSource.remote.attendance.AttendanceRemoteDataSource
import com.umc.data.response.attendance.AdminPendingUserResponse.Companion.toAdminPendingUser
import com.umc.data.response.attendance.AttendanceAvailableResponse.Companion.toUserCheckAvailable
import com.umc.domain.model.act.check.AdminPendingUser
import com.umc.domain.model.act.check.UserCheckAvailable
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.map
import com.umc.domain.repository.attendance.AttendanceRepository
import javax.inject.Inject

class AttendanceRepositoryImpl @Inject constructor(
    private val attendanceRemoteDataSource: AttendanceRemoteDataSource
) : AttendanceRepository {

    override suspend fun getAttendanceAvailable(): ApiState<List<UserCheckAvailable>> {
        return attendanceRemoteDataSource.getAttendanceAvailable().map { responseList ->
            responseList.map { it.toUserCheckAvailable() }
        }
    }

    override suspend fun postAttendanceCheck(sheetId: Int): ApiState<String> {
        return attendanceRemoteDataSource.postAttendanceCheck(sheetId)
    }

    override suspend fun getPendingUsers(scheduleId: Int): ApiState<List<AdminPendingUser>> {
        return attendanceRemoteDataSource.getPendingUsers(scheduleId).map { responseList ->
            responseList.map { it.toAdminPendingUser() }
        }
    }
}