package com.umc.domain.usecase.schedule

import com.umc.domain.model.act.check.UserCheckAvailable
import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.schedule.ScheduleRepository
import javax.inject.Inject

class GetAttendanceAvailableSessionsUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) {
    suspend operator fun invoke(isAttendanceRequired: Boolean = true): ApiState<List<UserCheckAvailable>> =
        scheduleRepository.getAttendanceAvailableSessions(isAttendanceRequired)
}
