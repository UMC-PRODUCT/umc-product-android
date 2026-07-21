package com.umc.data.request.schedule

import com.google.gson.annotations.SerializedName

//d일정 수정을 위해 사용할 req입니다. (생성과 다름)
data class UpdateScheduleRequest(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("tags") val tags: List<String>,
    @SerializedName("startsAt") val startsAt: String,
    @SerializedName("endsAt") val endsAt: String,
    @SerializedName("location") val location: LocationRequest?,
    @SerializedName("isOnline") val isOnline: Boolean?,
    @SerializedName("isAttendanceRequired") val isAttendanceRequired: Boolean?,
    @SerializedName("attendancePolicy") val attendancePolicy: AttendancePolicyRequest?,
    @SerializedName("participantMemberIds") val participantMemberIds: List<Long>
) {
    data class LocationRequest(
        @SerializedName("latitude") val latitude: Double,
        @SerializedName("longitude") val longitude: Double,
        @SerializedName("locationName") val locationName: String
    )

    data class AttendancePolicyRequest(
        @SerializedName("checkInStartAt") val checkInStartAt: String,
        @SerializedName("onTimeEndAt") val onTimeEndAt: String,
        @SerializedName("lateEndAt") val lateEndAt: String
    )
}