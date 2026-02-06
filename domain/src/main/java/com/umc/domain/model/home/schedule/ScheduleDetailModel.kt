package com.umc.domain.model.home.schedule

data class ScheduleDetailModel (
    val scheduleId: Int,
    val name: String,
    val description: String,
    val tags: List<String>,
    val startDay: String,   // "2026.02.05"
    val startTime: String,  // "06:20"
    val endDay: String,     // "2026.02.05"
    val endTime: String,    // "06:20"
    val isAllDay: Boolean,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val status: String,
    val dDay: String,
    val requiresAttendanceApproval: Boolean
)