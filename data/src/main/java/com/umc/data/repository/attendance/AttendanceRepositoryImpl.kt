package com.umc.data.repository.attendance

import com.umc.data.dataSource.remote.attendance.AttendanceRemoteDataSource
import com.umc.data.response.attendance.AttendanceAvailableResponse.Companion.toModel
import com.umc.domain.model.act.check.UserCheckAvailable
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.map
import com.umc.domain.repository.attendance.AttendanceRepository
import javax.inject.Inject

class AttendanceRepositoryImpl @Inject constructor(
    private val attendanceRemoteDataSource: AttendanceRemoteDataSource
) : AttendanceRepository {

    override suspend fun getAttendanceAvailable(): ApiState<UserCheckAvailable> {
        return attendanceRemoteDataSource.getAttendanceAvailable().map { it.toModel() }
    }
}