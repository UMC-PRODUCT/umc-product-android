package com.umc.data.request.schedule

import com.google.gson.annotations.SerializedName

data class CreateScheduleRequest(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("tags") val tags: List<String>,
    @SerializedName("startsAt") val startsAt: String,
    @SerializedName("endsAt") val endsAt: String,
    // [변경] 기존 locationName/latitude/longitude 분리 → 중첩 객체로 통합
    //        null 전송 시 비대면 일정으로 간주됨
    @SerializedName("location") val location: LocationRequest?,
    // [추가] 출석 체크 정책 (운영진만 입력 가능, 일반 챌린저는 null)
    @SerializedName("attendancePolicy") val attendancePolicy: AttendancePolicyRequest?,
    @SerializedName("participantMemberIds") val participantMemberIds: List<Long>
    // [제거] isAllDay: 하루종일 여부는 startsAt/endsAt으로 표현
    //        (KST 기준 00:00:000 ~ 23:59:999 → UTC-Based ISO8601 변환 후 전송)
    // [제거] gisuId: v2 API에서 제거됨
    // [제거] requiresApproval: v2 API에서 제거됨
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
