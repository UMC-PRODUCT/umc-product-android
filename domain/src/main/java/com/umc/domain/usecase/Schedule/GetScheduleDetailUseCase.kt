package com.umc.domain.usecase.Schedule

import com.umc.domain.repository.schedule.ScheduleRepository
import javax.inject.Inject

class GetScheduleDetailUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) {
    suspend operator fun invoke(scheduleId: Int) =
        scheduleRepository.getScheduleDetailHome(scheduleId)

}