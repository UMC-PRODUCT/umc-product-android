package com.umc.domain.usecase.schedule

import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.schedule.ScheduleRepository
import javax.inject.Inject

class PostScheduleAttendanceExcuseUseCase @Inject constructor(
    private val repository: ScheduleRepository
) {
    suspend operator fun invoke(
        scheduleId: Long,
        excuseReason: String,
        isVerified: Boolean,
        latitude: Double?,
        longitude: Double?
    ): ApiState<Unit> {
        return repository.postAttendanceExcuse(scheduleId, excuseReason, isVerified, latitude, longitude)
    }
}
