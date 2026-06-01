package com.umc.data.request.schedule

import com.google.gson.annotations.SerializedName

data class ScheduleAttendanceRequest(
    @SerializedName("locationVerified") val locationVerified: Boolean,
    @SerializedName("latitude") val latitude: Double?,
    @SerializedName("longitude") val longitude: Double?
)
