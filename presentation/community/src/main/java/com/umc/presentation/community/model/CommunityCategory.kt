package com.umc.presentation.community.model

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