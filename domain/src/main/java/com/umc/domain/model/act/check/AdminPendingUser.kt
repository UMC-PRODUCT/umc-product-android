package com.umc.domain.model.act.check

data class AdminPendingUser(
    val id: Int,
    val name: String,
    val university: String,
    val requestTime: String,        // 요청 시간 (예: "14:05")
    val hasLateReason: Boolean,     // 지각 사유 존재 여부 (! 아이콘 표시용)
    val lateReason: String? = null  // 상세 사유 내용 (팝업용)
)