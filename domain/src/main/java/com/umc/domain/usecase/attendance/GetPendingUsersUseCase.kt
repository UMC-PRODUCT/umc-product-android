package com.umc.domain.usecase.attendance

import com.umc.domain.model.act.check.AdminPendingUser
import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.schedule.ScheduleRepository
import javax.inject.Inject

class GetPendingUsersUseCase @Inject constructor(
    private val repository: ScheduleRepository
) {
    suspend operator fun invoke(scheduleId: Long): ApiState<List<AdminPendingUser>> =
        repository.getPendingUsers(scheduleId)
}