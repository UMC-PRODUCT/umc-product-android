package com.umc.domain.model.enums

enum class AdminSessionStatus(val text: String) {
    IN_PROGRESS("진행중"),
    COMPLETED("종료됨");

    companion object {
        fun fromServerValue(value: String): AdminSessionStatus {
            return when (value) {
                "IN_PROGRESS" -> IN_PROGRESS
                "COMPLETED" -> COMPLETED
                else -> COMPLETED
            }
        }
    }
}
