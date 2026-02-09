package com.umc.domain.usecase.schedule

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.home.schedule.UpdateSchedule
import com.umc.domain.repository.schedule.ScheduleRepository
import javax.inject.Inject

class UpdateScheduleUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository
)  {
    suspend operator fun invoke(scheduleId: Long, request: UpdateSchedule): ApiState<Unit>
        = scheduleRepository.updateSchedule(scheduleId, request)
    }
