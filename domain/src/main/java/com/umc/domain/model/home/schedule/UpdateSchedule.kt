package com.umc.domain.model.home.schedule

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
)
