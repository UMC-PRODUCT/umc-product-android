package com.umc.domain.model.enums

/** 공지 작성 카테고리 종류 */
enum class WriteCategoryType {
    ALL_GISU,           // 전체 기수 (최고 관리자 전용)
    GISU,               // 특정 기수 (최고 관리자 전용)
    CENTRAL_STAFF,      // 중앙운영진
    // CHAPTER_PRESIDENT,  // 지부장 — 서버 noticeTab에 지부 등급이 없어 보류
    SCHOOL_CORE,        // 학교 회장단
    SCHOOL_PART_LEADER, // 학교 파트장
}
