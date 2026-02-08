package com.umc.domain.usecase.attendance

import com.umc.domain.repository.attendance.AttendanceRepository
import javax.inject.Inject

class PostAttendanceApprovalUseCase @Inject constructor(private val repository: AttendanceRepository) {
    suspend operator fun invoke(recordId: Int) = repository.approveAttendance(recordId)
}