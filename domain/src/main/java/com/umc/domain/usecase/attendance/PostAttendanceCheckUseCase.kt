package com.umc.domain.usecase.attendance

import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.attendance.AttendanceRepository
import javax.inject.Inject

class PostAttendanceCheckUseCase @Inject constructor(
    private val repository: AttendanceRepository
) {
    suspend operator fun invoke(sheetId: Int): ApiState<String> {
        return repository.postAttendanceCheck(sheetId)
    }
}