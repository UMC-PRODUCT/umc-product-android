package com.umc.data.response.notice

import com.umc.data.response.notice.NoticeSummaryResponse.Companion.toModel
import com.umc.data.response.notice.NoticeTargetResponse.Companion.toModel
import com.umc.domain.model.notice.NoticeSearch
import com.umc.domain.model.notice.NoticeTarget
import com.umc.domain.model.notice.NoticeSummary
import kotlinx.serialization.Serializable

@Serializable
data class NoticeSearchResponse(
    val content: List<NoticeSummaryResponse>?,
    val page: Int?,
    val size: Int?,
    val totalElements: Int?,
    val totalPages: Int?,
    val hasNext: Boolean?,
    val hasPrevious: Boolean?
) {
    companion object {
        fun NoticeSearchResponse.toModel(): NoticeSearch = NoticeSearch(
            content = content?.map { it.toModel() }.orEmpty(),
            page = page ?: 0,
            size = size ?: 0,
            totalElements = totalElements ?: 0,
            totalPages = totalPages ?: 0,
            hasNext = hasNext ?: false,
            hasPrevious = hasPrevious ?: false
        )
    }
}

@Serializable
data class NoticeSummaryResponse(
    val id: Long?,
    val title: String?,
    val content: String?,
    val shouldSendNotification: Boolean?,
    val mustRead: Boolean?,
    val viewCount: Int?,
    val createdAt: String?,
    val targetInfo: NoticeTargetResponse?,
    val authorChallengerId: Long?,
    val authorMemberId: Long?,
    val authorNickname: String?,
    val authorName: String?
) {
    companion object {
        fun NoticeSummaryResponse.toModel(): NoticeSummary = NoticeSummary(
            id = id ?: -1L,
            title = title.orEmpty(),
            content = content.orEmpty(),
            shouldSendNotification = shouldSendNotification ?: false,
            mustRead = mustRead ?: false,
            viewCount = viewCount ?: 0,
            createdAt = createdAt.orEmpty(),
            targetInfo = targetInfo?.toModel() ?: NoticeTarget(),
            authorChallengerId = authorChallengerId ?: 0,
            authorMemberId = authorMemberId ?: 0,
            authorNickname = authorNickname.orEmpty(),
            authorName = authorName.orEmpty()
        )
    }
}