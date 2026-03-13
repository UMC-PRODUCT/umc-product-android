package com.umc.domain.usecase.schedule

import com.umc.domain.model.act.check.AdminSessionCheck
import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.schedule.ScheduleRepository
import javax.inject.Inject

class GetAdminSessionListUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) {
    suspend operator fun invoke(): ApiState<List<AdminSessionCheck>> =
        scheduleRepository.getAdminScheduleList()
}