package com.umc.domain.model.act.challenger

import com.umc.domain.model.enums.CheckHistoryStatus
import com.umc.domain.model.enums.UserPart

/**
 * 다이얼로그의 출석/활동 기록 리스트 아이템 모델
 */
data class ChallengerInfoHistory(
    val title: String = "",
    val status: CheckHistoryStatus = CheckHistoryStatus.ABSENT
)

/**
 * 챌린저 상세 정보 다이얼로그 전체 데이터 모델
 */
data class ChallengerInfoDialogModel(
    val name: String = "알수없음",
    val university: String = "알수없음",
    val part: UserPart = UserPart.UNKNOWN,
    /** 인프라를 겸하는 챌린저면 파트 옆에 인프라 칩을 함께 보여준다. */
    val infra: Boolean = false,
    val generation: Int = 0,
    val profileImageUrl: String = "",
    val totalPoints: Double = 0.0,
    val history: List<ChallengerInfoHistory> = emptyList()
)
