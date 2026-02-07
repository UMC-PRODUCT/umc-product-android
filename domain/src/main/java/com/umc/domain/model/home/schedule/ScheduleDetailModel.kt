package com.umc.domain.model.home.schedule

import com.umc.domain.model.enums.CategoryType

data class ScheduleDetailModel (
    val scheduleId: Int,
    val name: String,
    val description: String,
    val tags: List<CategoryType>,
    val startDay: String,   // "2026.02.05"
    val startTime: String,  // "06:20"
    val endDay: String,     // "2026.02.05"
    val endTime: String,    // "06:20"
    val isAllDay: Boolean,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val status: String,
    val dDay: Int,
    val requiresAttendanceApproval: Boolean
)