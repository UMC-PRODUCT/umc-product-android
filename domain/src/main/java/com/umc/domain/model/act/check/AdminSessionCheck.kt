package com.umc.domain.model.act.check

import com.umc.domain.model.enums.AdminSessionStatus

data class AdminSessionCheck(
    val id: Long,
    val title: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val status: AdminSessionStatus,
    val attendanceRate: Int,        // 출석률
    val totalChallengers: Int,      // 전체 인원
    val attendedChallengers: Int,   // 출석 완료 인원
    val pendingCount: Int,          // 승인 대기 인원
    val pendingUsers: List<AdminPendingUser>,
    val sheetId: Long?
)