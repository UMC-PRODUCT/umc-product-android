package com.umc.domain.model.act.check

/**
 * 출석 가능 세션의 Enum값
 */
enum class CheckAvailableStatus(val text: String) {
    BEFORE("출석 전"),
    PENDING("승인 대기"),
    COMPLETED("출석 완료")
}

/**
 * 출석 기록의 Enumr값
 */
enum class CheckHistoryStatus(val text: String) {
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
    val admin: String,
    val status: CheckAvailableStatus,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val isLocationCertified: Boolean? = null
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
    val status: CheckHistoryStatus
)