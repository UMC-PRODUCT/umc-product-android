package com.umc.domain.usecase.attendance

import com.umc.domain.repository.attendance.AttendanceRepository
import javax.inject.Inject

class PostAttendanceRejectionUseCase @Inject constructor(private val repository: AttendanceRepository) {
    suspend operator fun invoke(recordId: Long) = repository.rejectAttendance(recordId)
}