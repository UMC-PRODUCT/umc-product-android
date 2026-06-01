package com.umc.domain.model.act.check

data class AttendanceDecision(
    val participantMemberId: Long,
    val isApproved: Boolean,
    val reason: String? = null
)
