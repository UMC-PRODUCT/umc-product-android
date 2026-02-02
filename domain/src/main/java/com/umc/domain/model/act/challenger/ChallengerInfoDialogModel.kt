package com.umc.domain.model.act.challenger

import com.umc.domain.model.enums.CheckHistoryStatus

/**
 * 다이얼로그의 출석/활동 기록 리스트 아이템 모델
 */
data class SimpleHistoryItem(
    val title: String,
    val status: CheckHistoryStatus
)

/**
 * 챌린저 상세 정보 다이얼로그 전체 데이터 모델
 */
data class ChallengerInfoDialogModel(
    val name: String,
    val university: String,
    val part: String,
    val generation: String = "12기",
    val warningCount: Double = 0.0,
    val history: List<SimpleHistoryItem> = emptyList()
)