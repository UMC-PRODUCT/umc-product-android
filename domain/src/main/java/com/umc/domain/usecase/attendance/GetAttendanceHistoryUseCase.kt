package com.umc.domain.usecase.attendance

import com.umc.domain.model.act.check.UserCheckHistory
import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.attendance.AttendanceRepository
import javax.inject.Inject

class GetAttendanceHistoryUseCase @Inject constructor(
    private val attendanceRepository: AttendanceRepository
) {
    suspend operator fun invoke(): ApiState<List<UserCheckHistory>> {
        return attendanceRepository.getAttendanceHistory()
    }
}