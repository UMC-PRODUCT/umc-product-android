package com.umc.domain.model.act.check

import com.umc.domain.model.enums.AdminSessionStatus

data class AdminSessionCheck(
    val id: Int,
    val title: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val adminStatus: AdminSessionStatus,
    val stats: AdminCheckStats,
    val pendingUsers: List<AdminPendingUser>
)