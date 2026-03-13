package com.umc.domain.model.notice

data class NoticeSearch(
    val content: List<NoticeSummary>,
    val page: Int,
    val size: Int,
    val totalElements: Int,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)

data class NoticeSummary(
    val id: Long,
    val title: String,
    val content: String,
    val shouldSendNotification: Boolean,
    val viewCount: Int,
    val createdAt: String,
    val targetInfo: NoticeTarget,
    val authorChallengerId: Long,
    val authorNickname: String,
    val authorName: String
)