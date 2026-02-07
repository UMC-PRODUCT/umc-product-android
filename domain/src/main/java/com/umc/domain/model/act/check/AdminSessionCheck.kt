package com.umc.domain.model.act.check

data class AdminSessionCheck(
    val id: Int,
    val title: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val status: String,
    val attendanceRate: Int,        // 출석률
    val totalChallengers: Int,      // 전체 인원
    val attendedChallengers: Int,   // 출석 완료 인원
    val pendingCount: Int,          // 승인 대기 인원
    val pendingUsers: List<AdminPendingUser>
)