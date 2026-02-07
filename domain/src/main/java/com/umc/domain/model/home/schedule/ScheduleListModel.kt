package com.umc.domain.model.home.schedule

data class ScheduleListModel (
    val scheduleId: Int,
    val name: String,
    val status: String,
    val date: String,        // "2026.02.05"
    val dayOfWeek: String,   // "THU"
    val startTime: String,
    val endTime: String,
    val locationName: String,
    val totalCount: Int,
    val presentCount: Int,
    val pendingCount: Int,
    val attendanceRate: Double,
)