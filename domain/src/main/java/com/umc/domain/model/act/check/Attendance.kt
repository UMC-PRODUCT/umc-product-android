package com.umc.domain.model.act.check

enum class AttendanceStatus(val text: String) {
    BEFORE("출석 전"),
    SUCCESS("출석"),
    LATE("지각"),
    ABSENT("결석")
}

/**
 * 현재 출석 가능한 세션 정보
 */
data class CheckAvailable(
    val id: Int,
    val title: String,
    val startTime: String,
    val endTime: String,
    val status: AttendanceStatus
)

/**
 * 과거 출석 기록 정보
 */
data class CheckHistory(
    val id: Int,
    val week: String,
    val title: String,
    val startTime: String,
    val endTime: String,
    val status: AttendanceStatus
)