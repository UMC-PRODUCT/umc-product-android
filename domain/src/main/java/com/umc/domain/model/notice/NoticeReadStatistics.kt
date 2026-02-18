package com.umc.domain.model.notice

data class NoticeReadStatistics(
    val totalCount: Int,
    val readCount: Int,
    val unreadCount: Int,
    val readRate: Double
)