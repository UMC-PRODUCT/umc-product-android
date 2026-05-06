package com.umc.domain.model.enums

enum class CheckHistoryStatus(val text: String) {
    PRESENT("출석"),
    LATE("지각"),
    ABSENT("결석"),
    EXCUSED("사유 출석"),
    PRESENT_PENDING("출석 대기");

    companion object {
        fun fromServerValue(value: String?): CheckHistoryStatus = when (value) {
            "PRESENT" -> PRESENT
            "LATE" -> LATE
            "ABSENT" -> ABSENT
            "EXCUSED" -> EXCUSED
            "PRESENT_PENDING", "LATE_PENDING", "EXCUSED_PENDING" -> PRESENT_PENDING
            else -> ABSENT
        }
    }
}