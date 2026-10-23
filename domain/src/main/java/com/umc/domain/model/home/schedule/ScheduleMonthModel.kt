package com.umc.domain.model.home.schedule

import com.umc.domain.model.enums.CategoryType

data class ScheduleMonthModel (
    val scheduleId: Long,
    val name: String,
    val startDay: String,   // "2026.02.07"
    val startTime: String,  // "05:24"
    val endDay: String,     // "2026.02.08"
    val endTime: String,    // "05:24"
    val status: String,
    val dDay: Int,
    val tags: List<CategoryType> = emptyList() // 일정 태그(카테고리)
)