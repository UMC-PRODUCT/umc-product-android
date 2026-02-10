package com.umc.domain.usecase.schedule

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.home.schedule.CreateSchedule
import com.umc.domain.repository.schedule.ScheduleRepository
import javax.inject.Inject

class CreateScheduleUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) {
    suspend operator fun invoke(request: CreateSchedule): ApiState<Long> {
        return scheduleRepository.createSchedule(request)
    }
}