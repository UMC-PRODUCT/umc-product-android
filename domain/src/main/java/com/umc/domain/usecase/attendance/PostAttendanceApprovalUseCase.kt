package com.umc.domain.usecase.attendance

import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.attendance.AttendanceRepository
import javax.inject.Inject

class PostAttendanceApprovalUseCase @Inject constructor(
    private val repository: AttendanceRepository
) {
    suspend operator fun invoke(recordIds: List<Long>): ApiState<Unit> {
        recordIds.forEach { id ->
            val result = repository.approveAttendance(id)
            if (result is ApiState.Fail) return result
        }
        return ApiState.Success(Unit)
    }
}