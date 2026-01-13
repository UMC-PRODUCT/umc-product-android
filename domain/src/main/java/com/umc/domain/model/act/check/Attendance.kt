package com.umc.domain.model.act.check

enum class AttendanceStatus(val text: String) {
    BEFORE("출석 전"),
    SUCCESS("출석"),
    LATE("지각"),
    ABSENT("결석")
}

data class AvailableSession(
    val id: Int,
    val title: String,
    val startTime: String,
    val endTime: String,
    val status: AttendanceStatus
)

data class AttendanceHistory(
    val id: Int,
    val week: String,
    val title: String,
    val startTime: String,
    val endTime: String,
    val status: AttendanceStatus
)