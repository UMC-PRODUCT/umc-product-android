package com.umc.domain.model.act.check

import com.umc.domain.model.enums.CheckHistoryStatus

/**
 * 과거 출석 기록 정보
 */
data class UserCheckHistory(
    val id: Int,
    val week: String,
    val title: String,
    val startTime: String,
    val endTime: String,
    val status: CheckHistoryStatus
)