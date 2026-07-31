package com.umc.presentation.community.model

enum class CommunityCategory(
    val label: String,
) {
    ALL("전체"),
    UNREAD("안읽음"),
    PART_NOTICE("파트공지"),
    STUDY("스터디"),
    QUESTION("질문"),
    FREE("자유"),
}

