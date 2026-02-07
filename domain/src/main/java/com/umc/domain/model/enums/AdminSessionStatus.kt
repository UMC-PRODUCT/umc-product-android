package com.umc.domain.model.enums

enum class AdminSessionStatus(val text: String) {
    IN_PROGRESS("진행 중"),
    COMPLETED("종료됨");

    companion object {
        fun fromServerValue(value: String): AdminSessionStatus {
            return when (value) {
                "진행 중", "예정" -> IN_PROGRESS
                "종료됨" -> COMPLETED
                else -> COMPLETED // 예외 상황 대비 기본값
            }
        }
    }
}