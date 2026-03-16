package com.umc.domain.model.act.challenger

import com.umc.domain.model.enums.PointType

data class ChallengerManageDialogModel(
    val challengerId: Long = 0L,
    val name: String = "",
    val nickname: String = "",
    val university: String = "",
    val part: String = "",
    val gisu: Int = 0,
    val profileImageUrl: String = "",
    val totalScore: Double = 0.0,
    val positiveCount: Int = 0,
    val warningCount: Int = 0,
    val history: List<ChallengerPoint> = emptyList()
)

data class ChallengerPoint(
    val id: Long,
    val date: String = "",
    val title: String,
    val pointType: PointType,
    val value: Double
)