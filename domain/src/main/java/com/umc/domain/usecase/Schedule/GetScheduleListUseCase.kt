package com.umc.domain.usecase.Schedule

import com.umc.domain.repository.schedule.ScheduleRepository
import javax.inject.Inject

class GetScheduleListUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) {
    suspend operator fun invoke() =
        scheduleRepository.getScheduleList()
}