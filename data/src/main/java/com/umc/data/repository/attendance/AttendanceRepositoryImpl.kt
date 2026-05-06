package com.umc.data.repository.attendance

import com.umc.data.dataSource.remote.attendance.AttendanceRemoteDataSource
import com.umc.data.response.attendance.ChallengerAttendanceHistoryResponse.Companion.toChallengerInfoHistory
import com.umc.domain.model.act.challenger.ChallengerInfoHistory
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.map
import com.umc.domain.repository.attendance.AttendanceRepository
import javax.inject.Inject

class AttendanceRepositoryImpl @Inject constructor(
    private val attendanceRemoteDataSource: AttendanceRemoteDataSource
) : AttendanceRepository {

    override suspend fun getChallengerAttendanceHistory(challengerId: Long): ApiState<List<ChallengerInfoHistory>> {
        return attendanceRemoteDataSource.getChallengerAttendanceHistory(challengerId).map { responseList ->
            responseList.map { it.toChallengerInfoHistory() }
        }
    }
}