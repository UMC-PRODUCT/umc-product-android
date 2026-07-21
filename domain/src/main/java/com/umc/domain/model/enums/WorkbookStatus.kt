package com.umc.domain.model.enums

enum class WorkbookStatus {
    PENDING,        // 기본
    IN_PROGRESS,    // 진행 중
    SUBMITTED,      // 제출 완료
    PASS,           // 통과
    FAIL,           // 실패
    BEST,
    UNKNOWN;

    companion object {
        fun from(value: String?): WorkbookStatus {
            if (value.isNullOrBlank()) return UNKNOWN
            return entries.firstOrNull { it.name == value } ?: UNKNOWN
        }
    }
}
