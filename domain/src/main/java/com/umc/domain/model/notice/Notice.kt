package com.umc.domain.model.notice

import com.umc.domain.model.enums.NoticeCategory

data class Notice(
    val id: Int,
    val isMustRead: Boolean,
    val category: NoticeCategory,
    val date: String,
    val title: String,
    val content: String,
    val author: String,
    val count: Int // 조회수
)
