package com.umc.presentation.community.chatting

import com.umc.domain.model.community.thread.CommunityThreadDetail
import com.umc.domain.model.community.thread.CommunityThreadRole
import com.umc.presentation.community.edit.CommunityEditState
import org.junit.Assert.*
import org.junit.Test

class CommunityLastOwnerLeaveTest {
    @Test fun editScreenUsesTheServerTotalIncludingTheOwner() {
        val state = CommunityEditState(canEdit = true, isLoading = false, totalMemberCount = 1)
        assertTrue(state.isLastRemainingOwner)
        assertFalse(state.copy(totalMemberCount = 2, currentChallengerCount = 0).isLastRemainingOwner)
        assertFalse(state.copy(totalMemberCount = null).isLastRemainingOwner)
        assertFalse(state.copy(isLoading = true).isLastRemainingOwner)
        assertFalse(state.copy(canEdit = false).isLastRemainingOwner)
    }

    private fun state(role: CommunityThreadRole, count: String) = CommunityChattingState(
        threadId = "thread", thread = CommunityThreadDetail(myRole = role, memberCount = count),
    )

    @Test fun deletionNoticeIsOnlyForTheLastOwner() {
        assertTrue(state(CommunityThreadRole.OWNER, "1").isLastRemainingOwner)
        assertFalse(state(CommunityThreadRole.OWNER, "2").isLastRemainingOwner)
        assertFalse(state(CommunityThreadRole.MEMBER, "1").isLastRemainingOwner)
        assertFalse(state(CommunityThreadRole.ADMIN, "1").isLastRemainingOwner)
    }

    @Test fun missingOrInvalidCountDoesNotShowADeletionNotice() {
        assertFalse(CommunityChattingState(threadId = "thread").isLastRemainingOwner)
        listOf("", "unknown", "0", "-1").forEach {
            assertFalse(state(CommunityThreadRole.OWNER, it).isLastRemainingOwner)
        }
    }
}
