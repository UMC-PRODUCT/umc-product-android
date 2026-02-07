package com.umc.domain.model.community

data class LightningInfo(
    val meetAt: String,         // 모임 일시
    val location: String,       // 장소 명칭
    val maxParticipants: Int    // 모집 정원
)