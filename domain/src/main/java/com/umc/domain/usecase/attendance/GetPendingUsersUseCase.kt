package com.umc.domain.usecase.attendance

import com.umc.domain.model.act.check.AdminPendingUser
import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.attendance.AttendanceRepository
import javax.inject.Inject

class GetPendingUsersUseCase @Inject constructor(
    private val repository: AttendanceRepository
) {
    suspend operator fun invoke(scheduleId: Int): ApiState<List<AdminPendingUser>> =
        repository.getPendingUsers(scheduleId)
}