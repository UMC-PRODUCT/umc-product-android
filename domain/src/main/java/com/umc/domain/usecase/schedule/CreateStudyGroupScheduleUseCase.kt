package com.umc.domain.usecase.schedule

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.home.schedule.CreateStudyGroupSchedule
import com.umc.domain.repository.schedule.ScheduleRepository
import javax.inject.Inject

class CreateStudyGroupScheduleUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) {
    suspend operator fun invoke(request: CreateStudyGroupSchedule): ApiState<Long> {
        return scheduleRepository.createStudyGroupSchedule(request)
    }
}