package com.umc.domain.model.enums

/** 공지 작성 카테고리 종류 */
enum class WriteCategoryType {
    ALL_GISU,           // 전체 기수 (Super Admin·총괄단)
    GISU,               // 특정 기수 (Super Admin·총괄단·중앙 운영진·지부장). 지부장은 전용 noticeTab 없이 본인 지부의 일반 공지로 발행
    CENTRAL_STAFF,      // 중앙운영진
    SCHOOL_CORE,        // 학교 회장단
    SCHOOL_PART_LEADER, // 학교 파트장
}
