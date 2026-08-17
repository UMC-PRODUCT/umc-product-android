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
    val mustRead: Boolean = false,
    val viewCount: Int,
    val createdAt: String,
    val targetInfo: NoticeTarget,
    val authorChallengerId: Long,
    val authorMemberId: Long = -1L,
    val authorNickname: String,
    val authorName: String
)