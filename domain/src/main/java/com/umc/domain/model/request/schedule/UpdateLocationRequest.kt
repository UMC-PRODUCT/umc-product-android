package com.umc.domain.model.request.schedule

data class UpdateLocationRequest(
    val locationName: String,
    val latitude: Double,
    val longitude: Double
)