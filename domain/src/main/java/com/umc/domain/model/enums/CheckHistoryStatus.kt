package com.umc.domain.model.enums

enum class CheckHistoryStatus(val text: String) {
    SUCCESS("출석"),
    LATE("지각"),
    ABSENT("결석")
}