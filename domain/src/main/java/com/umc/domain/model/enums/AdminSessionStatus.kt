package com.umc.domain.model.enums

import java.time.ZonedDateTime

enum class AdminSessionStatus(val text: String) {
    IN_PROGRESS("진행 중"),
    COMPLETED("종료됨");

    companion object {
        fun fromScheduleTimes(startsAt: String, endsAt: String): AdminSessionStatus {
            return try {
                val normalizedEnd = if (!endsAt.endsWith("Z")) "${endsAt}Z" else endsAt
                val end = ZonedDateTime.parse(normalizedEnd)
                if (ZonedDateTime.now().isBefore(end)) IN_PROGRESS else COMPLETED
            } catch (e: Exception) {
                COMPLETED
            }
        }

        fun fromServerValue(value: String): AdminSessionStatus {
            return when (value) {
                "진행 중", "예정", "IN_PROGRESS" -> IN_PROGRESS
                "종료됨", "COMPLETED" -> COMPLETED
                else -> COMPLETED
            }
        }
    }
}