package com.umc.presentation.community.model

/**
 * 커뮤니티 스레드에서 사용하는 카테고리
 *
 * 스레드 분류 및 목록 필터링에 사용되며,
 * 각 카테고리가 화면에 표시할 한글 라벨을 관리합니다.
 */
enum class CommunityCategory(
    val label: String,
) {
    ALL("전체"),
    UNREAD("안읽음"),

    STUDY("스터디"),
    QNA("질문"),
    PROJECT("프로젝트"),
    FREE("자유"),
}