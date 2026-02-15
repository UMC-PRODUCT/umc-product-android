package com.umc.data.request.schedule

import com.google.gson.annotations.SerializedName

//일정생성을 위해 필요한 req입니다.
data class CreateScheduleRequest (
    @SerializedName("name") val name: String,         // 이름
    @SerializedName("startsAt") val startsAt: String, // 시작 날짜 ("2026-02-08T09:57:19.628Z")
    @SerializedName("endsAt") val endsAt: String,     // 끝나는 날짜
    @SerializedName("isAllDay") val isAllDay: Boolean,// 하루종일이냐 (isAllDay)
    @SerializedName("locationName") val locationName: String,   // 위치
    @SerializedName("latitude") val latitude: Double,           // 위도
    @SerializedName("longitude") val longitude: Double,         // 경도
    @SerializedName("description") val description: String,     // 설명
    @SerializedName("participantMemberIds") val participantMemberIds: List<Long>, // 참여하는 유저 id들
    @SerializedName("tags") val tags: List<String>, // "STUDY", "PROJECT" 등 Enum String 리스트
    @SerializedName("gisuId") val gisuId: Long,     // 기수 id  TODO. 차후 들어오면 반영 일단 1로
    @SerializedName("requiresApproval") val requiresApproval: Boolean   // 운영진이면 true 그 외면 false
)