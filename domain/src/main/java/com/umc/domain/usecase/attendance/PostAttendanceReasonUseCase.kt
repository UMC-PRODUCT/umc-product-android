package com.umc.domain.usecase.attendance

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.attendance.AttendanceReasonRequest
import com.umc.domain.repository.attendance.AttendanceRepository
import javax.inject.Inject

class PostAttendanceReasonUseCase @Inject constructor(
    private val repository: AttendanceRepository
) {
    suspend operator fun invoke(attendanceSheetId: Long, reason: String): ApiState<String> {
        return repository.postAttendanceReason(
            AttendanceReasonRequest(attendanceSheetId, reason)
        )
    }
}