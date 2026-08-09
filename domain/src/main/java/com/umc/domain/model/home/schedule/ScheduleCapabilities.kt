package com.umc.domain.model.home.schedule

data class ScheduleCapabilities(
    val canCreateSchedule: Boolean,
    val canCreateAttendanceRequiredSchedule: Boolean,
    val maxParticipantCount: Int
)
