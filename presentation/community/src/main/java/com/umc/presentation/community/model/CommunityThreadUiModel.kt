package com.umc.presentation.community.model

data class CommunityThreadUiModel(
    val id: Long,
    val title: String,
    val contentPreview: String,
    val category: CommunityCategory,
    val dayText: String,
    val commentCount: Int,
    val isPinned: Boolean = false,
    val isRead: Boolean = false,
)