package com.umc.domain.model.enums

//일정 생성 화면 카테고리
enum class CategoryType(val label: String) {
    NETWORKING("네트워킹"),
    PROJECT("프로젝트"),
    DUES("회비"),
    MEETING("회의"),
    ORIENTATION("오리엔테이션"),
    PRESENTATION("발표"),
    RETROSPECTIVE("회고"),
    GENERAL("일반"),
    LEADERSHIP("리더십"),
    STUDY("스터디"),
    HACKATHON("해커톤"),
    WORKSHOP("워크샵"),
    AFTER_PARTY("뒷풀이")
}
