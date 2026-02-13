package com.umc.data.response.schedule

data class UpdateLocationResponse(
    val scheduleId: Long,
    val locationName: String,
    val latitude: Double,
    val longitude: Double
)