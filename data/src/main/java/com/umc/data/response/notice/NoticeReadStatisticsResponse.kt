package com.umc.data.response.notice

data class NoticeReadStatisticsResponse(
    val totalCount: Int,
    val readCount: Int,
    val unreadCount: Int,
    val readRate: Double
)