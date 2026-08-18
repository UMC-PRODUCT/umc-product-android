package com.umc.data.response.community

data class CommunityThreadSummaryResponse(
    val threadId: String,
    val title: String,
    val description: String?,
    val category: String,
    val icon: String,
    val memberCount: String,
    val unreadCount: String,
    val maxMembers: String,
    val isPinned: Boolean,
    val isMuted: Boolean,
    val isJoined: Boolean,
    val myRole: String?,
    val lastMessage: CommunityThreadLastMessageResponse?,
    val createdBy: String,
    val createdAt: String,
    val updatedAt: String,
)
