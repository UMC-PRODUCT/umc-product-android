package com.umc.domain.usecase.schedule

import com.umc.domain.repository.schedule.ScheduleRepository
import javax.inject.Inject

class GetScheduleMonthUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) {
    suspend operator fun invoke(year: Int, month: Int) =
        scheduleRepository.getMonthSchedule(year, month)

}