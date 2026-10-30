package com.umc.presentation.community.chatting

import com.umc.domain.model.community.thread.CommunityThreadRole

internal data class CommunityParticipantMenuPermissions(
    val canTransfer: Boolean,
    val canChangeAdmin: Boolean,
    val canKick: Boolean,
)

/** Profile viewing is available for everyone; membership changes follow server role rules. */
internal fun communityParticipantMenuPermissions(
    viewerRole: CommunityThreadRole,
    targetRole: CommunityThreadRole,
    isMe: Boolean,
): CommunityParticipantMenuPermissions {
    val eligibleTarget = !isMe && targetRole in setOf(CommunityThreadRole.ADMIN, CommunityThreadRole.MEMBER)
    val isOwner = viewerRole == CommunityThreadRole.OWNER
    return CommunityParticipantMenuPermissions(
        canTransfer = isOwner && eligibleTarget,
        canChangeAdmin = isOwner && eligibleTarget,
        canKick = viewerRole in setOf(CommunityThreadRole.OWNER, CommunityThreadRole.ADMIN) && eligibleTarget,
    )
}
