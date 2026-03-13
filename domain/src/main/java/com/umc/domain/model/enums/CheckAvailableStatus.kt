package com.umc.domain.model.enums

enum class CheckAvailableStatus(val text: String) {
    BEFORE("출석 전"),
    PENDING("승인 대기"),
    COMPLETED("출석 완료");

    companion object {
        fun fromServerValue(value: String?): CheckAvailableStatus {
            return when (value) {
                // 1. 출석 전
                "PENDING" -> BEFORE

                // 2. 승인 대기
                "PRESENT_PENDING", "LATE_PENDING", "EXCUSED_PENDING" -> PENDING

                // 3. 출석 완료 (결석 포함 처리)
                "PRESENT", "LATE", "EXCUSED", "ABSENT" -> COMPLETED

                else -> BEFORE // 기본값
            }
        }
    }
}