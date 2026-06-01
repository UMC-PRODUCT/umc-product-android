package com.umc.data.request.schedule

import com.google.gson.annotations.SerializedName

data class DecideAttendanceRequest(
    @SerializedName("participantMemberId") val participantMemberId: Long,
    @SerializedName("isApproved") val isApproved: Boolean,
    @SerializedName("reason") val reason: String? = null
)
