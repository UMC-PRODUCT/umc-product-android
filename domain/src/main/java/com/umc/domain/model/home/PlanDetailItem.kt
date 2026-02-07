package com.umc.domain.model.home

import com.umc.domain.model.enums.CategoryType

data class PlanDetailItem (
    val scheduleId: Long = 0L,
    val name: String = "",
    val description: String = "",
    val tags: List<CategoryType> = emptyList(),
    val startDay: String = "",   // "2026.02.05"
    val startTime: String = "",  // "06:20"
    val endDay: String = "",     // "2026.02.05"
    val endTime: String = "",    // "06:20"
    val isAllDay: Boolean = false,
    val locationName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val status: String = "",
    val dDay: Int = -1,
    val requiresAttendanceApproval: Boolean = false
)