package com.umc.presentation.community.model

data class CommunityThreadUiModel(
    val id: String,
    val title: String,
    val contentPreview: String,
    val category: CommunityCategory,
    val icon: String = "",
    val dayText: String,
    val memberCount: Int = 0,
    val unreadCount: Int = 0,
    val maxMembers: Int = 0,
    val isPinned: Boolean = false,
    val isNotificationEnabled: Boolean = true,
    val isMine: Boolean = false,
    val isJoined: Boolean,
) {
    val isRead: Boolean
        get() = unreadCount == 0
}