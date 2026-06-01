package com.umc.data.response.schedule

import com.google.gson.annotations.SerializedName
import com.umc.domain.model.home.schedule.ScheduleCapabilities

data class ScheduleCapabilitiesResponse(
    @SerializedName("canCreateSchedule") val canCreateSchedule: Boolean,
    @SerializedName("canCreateAttendanceRequiredSchedule") val canCreateAttendanceRequiredSchedule: Boolean,
    @SerializedName("maxParticipantCount") val maxParticipantCount: Int
) {
    companion object {
        fun ScheduleCapabilitiesResponse.toDomain() = ScheduleCapabilities(
            canCreateSchedule = canCreateSchedule,
            canCreateAttendanceRequiredSchedule = canCreateAttendanceRequiredSchedule,
            maxParticipantCount = maxParticipantCount
        )
    }
}
