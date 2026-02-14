package com.umc.data.response.notice

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
)

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
)