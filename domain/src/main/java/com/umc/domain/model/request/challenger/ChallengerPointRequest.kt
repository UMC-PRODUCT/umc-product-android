package com.umc.domain.model.request.challenger

import com.umc.domain.model.enums.PointType

data class ChallengerPointRequest (
    val pointType: PointType, // "BEST_WORKBOOK", "WARNING", "OUT"
    val description: String
)