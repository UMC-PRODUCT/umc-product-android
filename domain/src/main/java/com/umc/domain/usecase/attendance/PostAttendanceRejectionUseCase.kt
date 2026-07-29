package com.umc.domain.usecase.attendance

import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.attendance.AttendanceRepository
import javax.inject.Inject

class PostAttendanceRejectionUseCase @Inject constructor(
    private val repository: AttendanceRepository
) {
    suspend operator fun invoke(scheduleId: Long, memberIds: List<Long>): ApiState<Unit> =
        repository.decideAttendance(scheduleId, memberIds, approved = false)
}
