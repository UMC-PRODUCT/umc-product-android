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
    val canEdit: Boolean = false,
    val isJoined: Boolean,
    /**
     * 내가 강퇴된 스레드인지 여부
     *
     * TODO: 강퇴 상태 API 스펙 확정 후 매핑 연결 (현재는 항상 false)
     */
    val isKicked: Boolean = false,
) {
    val isRead: Boolean
        get() = unreadCount == 0
}