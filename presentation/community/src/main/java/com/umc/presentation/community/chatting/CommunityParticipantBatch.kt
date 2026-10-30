package com.umc.presentation.community.chatting

import com.umc.domain.model.community.thread.CommunityThreadMember
import com.umc.domain.model.community.thread.CommunityThreadMemberState
import com.umc.domain.model.community.thread.CommunityThreadRole
import kotlinx.coroutines.CancellationException

enum class CommunityParticipantBatchAction { ASSIGN_ADMIN, REMOVE_ADMIN, KICK }

internal fun communityParticipantBatchActions(
    members: Collection<CommunityThreadMember>,
    viewerRole: CommunityThreadRole,
    myMemberId: String,
): Set<CommunityParticipantBatchAction> {
    val targets = communityManageableParticipants(members, viewerRole, myMemberId)
    return buildSet {
        if (viewerRole == CommunityThreadRole.OWNER && targets.isNotEmpty()) {
            if (targets.all { it.role == CommunityThreadRole.MEMBER }) add(CommunityParticipantBatchAction.ASSIGN_ADMIN)
            if (targets.all { it.role == CommunityThreadRole.ADMIN }) add(CommunityParticipantBatchAction.REMOVE_ADMIN)
        }
        if (targets.isNotEmpty()) add(CommunityParticipantBatchAction.KICK)
    }
}

internal fun communityManageableParticipants(
    members: Collection<CommunityThreadMember>,
    viewerRole: CommunityThreadRole,
    myMemberId: String,
): List<CommunityThreadMember> = members.filter {
    it.state == CommunityThreadMemberState.ACTIVE &&
        communityParticipantMenuPermissions(viewerRole, it.role, it.memberId == myMemberId).canKick
}

internal data class CommunityParticipantBatchResult(
    val successfulIds: Set<String>,
    val failedIds: Set<String>,
)

/** Individual endpoints are applied sequentially; a partial failure never rolls back completed work. */
internal suspend fun applyCommunityParticipantBatch(
    memberIds: Set<String>,
    operation: suspend (String) -> Boolean,
): CommunityParticipantBatchResult {
    val succeeded = linkedSetOf<String>()
    val failed = linkedSetOf<String>()
    memberIds.forEach { id ->
        val success = try {
            operation(id)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            false
        }
        if (success) succeeded += id else failed += id
    }
    return CommunityParticipantBatchResult(succeeded, failed)
}
