package com.umc.domain.model.community

data class CommunityThreadDetail(
    val threadId: String,
    val title: String,
    val description: String,
    val category: CommunityThreadCategory,
    val icon: String,
    val memberCount: Int,
    val unreadCount: Int,
    val maxMembers: Int,
    val isPinned: Boolean,
    val isMuted: Boolean,
    val isJoined: Boolean,
    val myRole: CommunityThreadRole,
    val lastMessage: CommunityThreadLastMessage?,
    val createdBy: String,
    val createdAt: String,
    val updatedAt: String,
    val shareUrl: String,
    val deletedAt: String?,
) {
    val isMine: Boolean
        get() = myRole == CommunityThreadRole.OWNER

    val isNotificationEnabled: Boolean
        get() = !isMuted
}