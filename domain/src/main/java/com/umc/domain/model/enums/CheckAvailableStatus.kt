package com.umc.domain.model.enums

enum class CheckAvailableStatus(val text: String) {
    BEFORE("출석 전"),
    PENDING("승인 대기"),
    COMPLETED("출석 완료")
}