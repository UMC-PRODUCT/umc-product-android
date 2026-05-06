package com.umc.domain.usecase.schedule

import com.umc.domain.model.act.check.AdminSessionCheck
import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.schedule.ScheduleRepository
import javax.inject.Inject

class GetScheduleAttendanceHistoryUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) {
    suspend operator fun invoke(
        from: String? = null,
        to: String? = null,
        attendanceStatus: String? = null
    ): ApiState<List<AdminSessionCheck>> {
        return scheduleRepository.getAttendanceHistory(from, to, attendanceStatus)
    }
}
