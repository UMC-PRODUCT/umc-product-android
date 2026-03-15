package com.umc.data.request.schedule

import com.google.gson.annotations.SerializedName

//d일정 수정을 위해 사용할 req입니다. (생성과 다름)
data class UpdateScheduleRequest (
    @SerializedName("name") val name: String,         // 이름
    @SerializedName("startsAt") val startsAt: String, // 시작 날짜 ("2026-02-08T09:57:19.628Z")
    @SerializedName("endsAt") val endsAt: String,     // 끝나는 날짜
    @SerializedName("isAllDay") val isAllDay: Boolean,// 하루종일이냐 (isAllDay)
    @SerializedName("locationName") val locationName: String,   // 위치
    @SerializedName("latitude") val latitude: Double,           // 위도
    @SerializedName("longitude") val longitude: Double,         // 경도
    @SerializedName("description") val description: String,     // 설명
    @SerializedName("tags") val tags: List<String>,
    @SerializedName("participantMemberIds") val participantMemberIds: List<Long>,
)