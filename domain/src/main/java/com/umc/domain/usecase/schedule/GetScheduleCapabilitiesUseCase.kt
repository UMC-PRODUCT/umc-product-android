package com.umc.domain.usecase.schedule

import com.umc.domain.repository.schedule.ScheduleRepository
import javax.inject.Inject

class GetScheduleCapabilitiesUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) {
    suspend operator fun invoke() = scheduleRepository.getScheduleCapabilities()
}
