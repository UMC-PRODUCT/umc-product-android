package com.umc.domain.model.request.attendance

import kotlinx.serialization.Serializable

@Serializable
data class AttendanceCheckRequest(
    val attendanceSheetId: Int,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationVerified: Boolean = false
)