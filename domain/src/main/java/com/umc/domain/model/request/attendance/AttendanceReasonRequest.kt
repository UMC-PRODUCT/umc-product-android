package com.umc.domain.model.request.attendance

data class AttendanceReasonRequest(
    val attendanceSheetId: Long,
    val reason: String
)
