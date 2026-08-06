package com.umc.domain.usecase.schedule

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.home.schedule.ScheduleCapabilities
import com.umc.domain.repository.schedule.ScheduleRepository
import javax.inject.Inject

class GetScheduleCapabilities @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) {
    suspend operator fun invoke(): ApiState<ScheduleCapabilities> {
        return scheduleRepository.getScheduleCapabilities()
    }
}