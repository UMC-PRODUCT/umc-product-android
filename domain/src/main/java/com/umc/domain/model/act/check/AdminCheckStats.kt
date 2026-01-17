package com.umc.domain.model.act.check

data class AdminCheckStats(
    val attendanceRate: Int,        // 출석률 (예: 85)
    val totalChallengers: Int,      // 전체 인원 (예: 40)
    val attendedChallengers: Int,   // 출석 완료 인원 (예: 34)
    val pendingCount: Int           // 승인 대기 인원 (예: 3)
)