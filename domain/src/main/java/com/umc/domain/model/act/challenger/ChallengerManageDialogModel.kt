package com.umc.domain.model.act.challenger

import com.umc.domain.model.enums.PointType
import com.umc.domain.model.enums.UserPart

data class ChallengerManageDialogModel(
    val challengerId: Long = 0L,
    val name: String = "",
    val nickname: String = "",
    val university: String = "",
    val part: UserPart = UserPart.UNKNOWN,
    /** 인프라를 겸하는 챌린저면 파트 옆에 인프라 칩을 함께 보여준다. */
    val infra: Boolean = false,
    val gisu: Int = 0,
    /** 대상 기수 ID. 상벌점 권한은 기수(generation)가 아니라 이 값으로 판정한다. */
    val gisuId: Long? = null,
    /** 대상 회원의 프로필 학교 ID. 교내 회장단 권한 판정에 쓴다. */
    val schoolId: Long? = null,
    val profileImageUrl: String = "",
    val totalScore: Double = 0.0,
    // 구형 BEST_WORKBOOK(-0.5) 기록이 있어 정수로 자르지 않는다
    val rewardScore: Double = 0.0,
    val penaltyScore: Double = 0.0,
    val history: List<ChallengerPoint> = emptyList()
)

data class ChallengerPoint(
    val id: Long,
    val date: String = "",
    val title: String,
    val pointType: PointType,
    val value: Double
)
