package com.umc.data.request.schedule

import com.google.gson.annotations.SerializedName

data class CreateStudyGroupScheduleRequest(
    @SerializedName("name") val name: String,
    @SerializedName("startsAt") val startsAt: String,
    @SerializedName("endsAt") val endsAt: String,
    @SerializedName("isAllDay") val isAllDay: Boolean,
    @SerializedName("locationName") val locationName: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("description") val description: String,
    @SerializedName("tags") val tags: List<String>,
    @SerializedName("studyGroupId") val studyGroupId: Long,
    @SerializedName("gisuId") val gisuId: Long,
    @SerializedName("requiresApproval") val requiresApproval: Boolean,
)