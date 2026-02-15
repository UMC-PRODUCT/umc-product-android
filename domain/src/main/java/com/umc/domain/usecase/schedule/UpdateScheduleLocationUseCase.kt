package com.umc.domain.usecase.schedule

import com.umc.domain.repository.schedule.ScheduleRepository
import javax.inject.Inject

class UpdateScheduleLocationUseCase @Inject constructor(
    private val repository: ScheduleRepository
) {
    suspend operator fun invoke(
        scheduleId: Long,
        locationName: String,
        latitude: Double,
        longitude: Double
    ) = repository.updateScheduleLocation(scheduleId, locationName, latitude, longitude)
}