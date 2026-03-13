package com.umc.domain.model.act.challenger

import com.umc.domain.model.enums.PointType

data class ChallengerManageDialogModel(
    val challengerId: Long = 0L,
    val name: String = "알수없음",
    val university: String = "알수없음",
    val part: String = "알수없음",
    val profileImageUrl: String = "",
    val hasNewAbsence: Boolean = false,
    val absenceCount: Int = 0,
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