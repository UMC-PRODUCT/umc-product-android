package com.umc.presentation.community.model

/**
 * 커뮤니티 스레드 목록에서 사용하는 UI 모델
 *
 * 스레드의 기본 정보와 카테고리, 참여 인원, 읽지 않은 메시지 수,
 * 고정 및 알림 설정 등의 화면 표시 상태를 관리합니다.
 */
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