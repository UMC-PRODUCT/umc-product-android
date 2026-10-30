package com.umc.presentation.community.chatting

import com.umc.domain.model.community.thread.CommunityThreadMember
import com.umc.domain.model.community.thread.CommunityThreadMemberState
import com.umc.domain.model.community.thread.CommunityThreadRole
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class CommunityParticipantBatchTest {
    @Test fun ownerCanRemoveSelectedAdminsAndAssignSelectedMembers() {
        val admin = CommunityThreadMember(memberId = "admin", role = CommunityThreadRole.ADMIN)
        val member = CommunityThreadMember(memberId = "member")
        assertEquals(setOf(CommunityParticipantBatchAction.REMOVE_ADMIN, CommunityParticipantBatchAction.KICK),
            communityParticipantBatchActions(listOf(admin), CommunityThreadRole.OWNER, "me"))
        assertEquals(setOf(CommunityParticipantBatchAction.ASSIGN_ADMIN, CommunityParticipantBatchAction.KICK),
            communityParticipantBatchActions(listOf(member), CommunityThreadRole.OWNER, "me"))
        assertEquals(setOf(CommunityParticipantBatchAction.KICK),
            communityParticipantBatchActions(listOf(admin, member), CommunityThreadRole.OWNER, "me"))
    }

    @Test fun adminRemovalIsUnavailableWithoutOwnerPermissionOrEligibleTargets() {
        val admin = CommunityThreadMember(memberId = "admin", role = CommunityThreadRole.ADMIN)
        assertEquals(setOf(CommunityParticipantBatchAction.KICK),
            communityParticipantBatchActions(listOf(admin), CommunityThreadRole.ADMIN, "me"))
        assertTrue(communityParticipantBatchActions(listOf(admin), CommunityThreadRole.MEMBER, "me").isEmpty())
        assertTrue(communityParticipantBatchActions(listOf(
            CommunityThreadMember(memberId = "me", role = CommunityThreadRole.ADMIN),
            CommunityThreadMember(memberId = "owner", role = CommunityThreadRole.OWNER),
            CommunityThreadMember(memberId = "left", role = CommunityThreadRole.ADMIN, state = CommunityThreadMemberState.LEFT),
        ), CommunityThreadRole.OWNER, "me").isEmpty())
    }

    @Test fun selectionExcludesSelfOwnerAndInactiveMembers() {
        val members = listOf(
            CommunityThreadMember(memberId = "me", role = CommunityThreadRole.ADMIN),
            CommunityThreadMember(memberId = "owner", role = CommunityThreadRole.OWNER),
            CommunityThreadMember(memberId = "admin", role = CommunityThreadRole.ADMIN),
            CommunityThreadMember(memberId = "member"),
            CommunityThreadMember(memberId = "left", state = CommunityThreadMemberState.LEFT),
        )
        assertEquals(listOf("admin", "member"), communityManageableParticipants(
            members, CommunityThreadRole.ADMIN, "me",
        ).map { it.memberId })
        assertTrue(communityManageableParticipants(members, CommunityThreadRole.MEMBER, "me").isEmpty())
    }

    @Test fun partialFailureKeepsCompletedWorkAndProcessesRemainingTargets() = runBlocking {
        val visited = mutableListOf<String>()
        val result = applyCommunityParticipantBatch(linkedSetOf("a", "b", "c", "d")) { id ->
            visited += id
            if (id == "b") throw IllegalStateException("network failure")
            id != "c"
        }
        assertEquals(listOf("a", "b", "c", "d"), visited)
        assertEquals(setOf("a", "d"), result.successfulIds)
        assertEquals(setOf("b", "c"), result.failedIds)
    }

    @Test fun cancellationDoesNotContinueWithMoreMembershipChanges() = runBlocking {
        val visited = mutableListOf<String>()
        try {
            applyCommunityParticipantBatch(linkedSetOf("a", "b", "c")) { id ->
                visited += id
                if (id == "b") throw CancellationException("screen disposed")
                true
            }
            fail("Cancellation must propagate")
        } catch (_: CancellationException) {
            assertEquals(listOf("a", "b"), visited)
        }
    }
}
