package com.umc.data.dataSource.remote.notice

/**
 * 공지 임시저장 응답.
 */
data class NoticeDraftDto(
    val noticeId: Long = 0L,
    val title: String = "",
    val body: String = "",
    val savedAt: String = "",
)
