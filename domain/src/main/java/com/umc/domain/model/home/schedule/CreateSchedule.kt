package com.umc.domain.model.home.schedule

/*
data class CreateSchedule (
    val name: String,                    // 일정 제목
    val startsAt: String,                // 시작 일시 (ISO 8601: "2026-02-08T09:57:19.628Z")
    val endsAt: String,                  // 종료 일시
    val isAllDay: Boolean,               // 종일 여부
    val locationName: String,            // 장소 명칭
    val latitude: Double,                // 위도
    val longitude: Double,               // 경도
    val description: String,             // 메모 및 상세 설명
    val participantMemberIds: List<Long>,// 참여자 Member ID 목록
    val tags: List<String>,              // 태그 목록 (예: "STUDY", "PROJECT")
    val gisuId: Long = 1L,               // 기수 ID (기본값 1)
    val requiresApproval: Boolean        // 승인 필요 여부 (운영진 여부에 따라 설정)
)

 */

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