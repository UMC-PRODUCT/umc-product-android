package com.umc.domain.model.act.challenger

import com.umc.domain.model.enums.PointType
import com.umc.domain.model.enums.UserPart

data class ChallengerManageDialogModel(
    val challengerId: Long = 0L,
    val name: String = "",
    val nickname: String = "",
    val university: String = "",
    val part: UserPart = UserPart.UNKNOWN,
    val gisu: Int = 0,
    val profileImageUrl: String = "",
    val totalScore: Double = 0.0,
    val rewardScore: Int = 0,
    val penaltyScore: Int = 0,
    val history: List<ChallengerPoint> = emptyList()
)

data class ChallengerPoint(
    val id: Long,
    val date: String = "",
    val title: String,
    val pointType: PointType,
    val value: Double
)
