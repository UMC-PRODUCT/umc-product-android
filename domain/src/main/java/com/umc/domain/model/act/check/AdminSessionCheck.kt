package com.umc.domain.model.act.check

data class AdminSessionCheck(
    val session: UserCheckAvailable,
    val stats: AdminCheckStats,
    val pendingUsers: List<AdminPendingUser>,
    val isExpanded: Boolean = false // 리스트 확장 상태 관리
)