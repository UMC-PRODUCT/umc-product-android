package com.umc.data.request.schedule

import com.google.gson.annotations.SerializedName

data class ExcuseAttendanceRequest(
    @SerializedName("excuseReason") val excuseReason: String,
    @SerializedName("isVerified") val isVerified: Boolean,
    @SerializedName("latitude") val latitude: Double?,
    @SerializedName("longitude") val longitude: Double?
)
