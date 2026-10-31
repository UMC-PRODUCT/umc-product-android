package com.umc.domain.model.community

data class CommunityThread(
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
    val membershipState: CommunityThreadMembershipState? = null,
) {
    val isMine: Boolean
        get() = myRole == CommunityThreadRole.OWNER

    /** 강퇴됐지만 아직 확인하지 않은 스레드 (목록에 비활성 카드로 표시) */
    val isKicked: Boolean
        get() = membershipState == CommunityThreadMembershipState.KICKED

    /** 초대된 적 없는 스레드 (참여 중이 아니고 멤버십 이력도 없음). 목록·검색에 노출하지 않는다. */
    val isUninvited: Boolean
        get() = !isJoined && membershipState == null

    val isNotificationEnabled: Boolean
        get() = !isMuted
}