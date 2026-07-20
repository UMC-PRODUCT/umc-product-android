package com.umc.domain.model.enums

/** 공지 발행/조회에 쓰는 서버 noticeTab 스펙 */
enum class NoticeTab(val value: String) {
    CHALLENGER("CHALLENGER"),
    CENTRAL_MEMBER("CENTRAL_MEMBER"),
    SCHOOL_CORE("SCHOOL_CORE"),
    SCHOOL_PART_LEADER("SCHOOL_PART_LEADER"),
}
