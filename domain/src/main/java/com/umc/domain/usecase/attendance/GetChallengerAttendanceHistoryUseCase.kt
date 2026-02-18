package com.umc.domain.usecase.attendance

import com.umc.domain.model.act.challenger.ChallengerInfoHistory
import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.attendance.AttendanceRepository
import javax.inject.Inject

class GetChallengerAttendanceHistoryUseCase @Inject constructor(
    private val attendanceRepository: AttendanceRepository
) {
    suspend operator fun invoke(challengerId: Long): ApiState<List<ChallengerInfoHistory>> {
        return attendanceRepository.getChallengerAttendanceHistory(challengerId)
    }
}