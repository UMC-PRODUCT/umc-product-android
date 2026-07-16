package com.umc.domain.model.home.schedule
/*
data class UpdateSchedule(
    val name: String,                    // 일정 제목
    val startsAt: String,                // 시작 일시 (ISO 8601: "2026-02-08T09:57:19.628Z")
    val endsAt: String,                  // 종료 일시
    val isAllDay: Boolean,               // 종일 여부
    val locationName: String,            // 장소 명칭
    val latitude: Double,                // 위도
    val longitude: Double,               // 경도
    val description: String,             // 메모 및 상세 설명
    val tags: List<String>,              // 태그 목록 (예: "STUDY", "PROJECT")
    val participantMemberIds: List<Long>, //참여자 리스트
)

 */

data class UpdateSchedule(
    val name: String,
    val description: String,
    val tags: List<String>,
    val startsAt: String,
    val endsAt: String,
    // null = 기존 장소 유지, non-null = 장소 변경 (isOnline=false 와 함께 사용)
    val location: Location?,
    // null = 기존 상태 유지, true = 비대면 전환, false = 대면 전환(location 필수)
    val isOnline: Boolean? = null,
    // null = 기존 상태 유지, true = 출석 O(attendancePolicy 필수), false = 출석 X
    val isAttendanceRequired: Boolean?,
    val attendancePolicy: AttendancePolicy?,
    val participantMemberIds: List<Long>
) {
    data class Location(
        val latitude: Double,
        val longitude: Double,
        val locationName: String
    )

    data class AttendancePolicy(
        val checkInStartAt: String,
        val onTimeEndAt: String,
        val lateEndAt: String
    )
}