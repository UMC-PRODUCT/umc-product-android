package com.umc.domain.usecase.schedule

import com.umc.domain.model.act.check.AttendanceDecision
import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.schedule.ScheduleRepository
import javax.inject.Inject

class PostScheduleAttendanceDecideUseCase @Inject constructor(
    private val repository: ScheduleRepository
) {
    suspend operator fun invoke(scheduleId: Long, decisions: List<AttendanceDecision>): ApiState<Unit> {
        return repository.postAttendanceDecide(scheduleId, decisions)
    }
}
