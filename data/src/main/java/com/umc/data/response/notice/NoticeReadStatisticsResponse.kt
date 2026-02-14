package com.umc.data.response.notice

import com.umc.domain.model.notice.NoticeReadStatistics
import kotlinx.serialization.Serializable

@Serializable
data class NoticeReadStatisticsResponse(
    val totalCount: Int,
    val readCount: Int,
    val unreadCount: Int,
    val readRate: Double
) {
    companion object {
        fun NoticeReadStatisticsResponse.toModel(): NoticeReadStatistics = NoticeReadStatistics(
            totalCount = totalCount,
            readCount = readCount,
            unreadCount = unreadCount,
            readRate = readRate
        )
    }
}