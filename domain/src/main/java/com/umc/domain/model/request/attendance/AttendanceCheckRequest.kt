package com.umc.domain.model.request.attendance

import kotlinx.serialization.Serializable

@Serializable
data class AttendanceCheckRequest(
    val attendanceSheetId: Int = 0
)