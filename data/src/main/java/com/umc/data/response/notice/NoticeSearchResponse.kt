package com.umc.data.response.notice

import com.umc.data.response.notice.NoticeSummaryResponse.Companion.toModel
import com.umc.data.response.notice.NoticeTargetResponse.Companion.toModel
import com.umc.domain.model.notice.NoticeSearch
import com.umc.domain.model.notice.NoticeSummary
import kotlinx.serialization.Serializable

@Serializable
data class NoticeSearchResponse(
    val content: List<NoticeSummaryResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Int,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
) {
    companion object {
        fun NoticeSearchResponse.toModel(): NoticeSearch = NoticeSearch(
            content = content.map { it.toModel() },
            page = page,
            size = size,
            totalElements = totalElements,
            totalPages = totalPages,
            hasNext = hasNext,
            hasPrevious = hasPrevious
        )
    }
}

@Serializable
data class NoticeSummaryResponse(
    val id: Long,
    val title: String,
    val content: String,
    val shouldSendNotification: Boolean,
    val viewCount: Int,
    val createdAt: String,
    val targetInfo: NoticeTargetResponse,
    val authorChallengerId: Long,
    val authorNickname: String,
    val authorName: String
) {
    companion object {
        fun NoticeSummaryResponse.toModel(): NoticeSummary = NoticeSummary(
            id = id,
            title = title,
            content = content,
            shouldSendNotification = shouldSendNotification,
            viewCount = viewCount,
            createdAt = createdAt,
            targetInfo = targetInfo.toModel(),
            authorChallengerId = authorChallengerId,
            authorNickname = authorNickname,
            authorName = authorName
        )
    }
}