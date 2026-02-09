package com.umc.domain.usecase.attendance

import com.umc.domain.model.act.check.UserCheckAvailable
import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.attendance.AttendanceRepository
import javax.inject.Inject

class GetAttendanceAvailableUseCase @Inject constructor(
    private val attendanceRepository: AttendanceRepository
) {
    suspend operator fun invoke(): ApiState<List<UserCheckAvailable>> {
        return attendanceRepository.getAttendanceAvailable()
    }
}