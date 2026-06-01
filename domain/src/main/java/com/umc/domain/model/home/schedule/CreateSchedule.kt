package com.umc.domain.model.home.schedule

data class CreateSchedule(
    val name: String,
    val description: String,
    val tags: List<String>,
    val startsAt: String,           // UTC-Based ISO8601 ("2026-05-21T01:00:00Z")
    val endsAt: String,             // UTC-Based ISO8601
    // [변경] 기존 locationName/latitude/longitude 분리 → 중첩 객체로 통합
    //        null이면 비대면 일정으로 간주됨
    val location: Location?,
    // [추가] 출석 체크 정책 (운영진만 설정 가능, 일반 챌린저는 null)
    val attendancePolicy: AttendancePolicy?,
    val participantMemberIds: List<Long>
    // [제거] isAllDay: startsAt/endsAt으로 표현 (KST 00:00:000~23:59:999 → UTC 변환)
    // [제거] gisuId
    // [제거] requiresApproval
) {
    data class Location(
        val latitude: Double,
        val longitude: Double,
        val locationName: String
    )

    data class AttendancePolicy(
        val checkInStartAt: String,  // UTC-Based ISO8601
        val onTimeEndAt: String,     // UTC-Based ISO8601
        val lateEndAt: String        // UTC-Based ISO8601
    )
}
