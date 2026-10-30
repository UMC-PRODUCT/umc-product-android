package com.umc.presentation.community.chatting

import com.umc.domain.model.community.thread.CommunityThreadRole
import org.junit.Assert.*
import org.junit.Test

class CommunityParticipantMenuPermissionsTest {
    @Test fun normalParticipantsHaveNoManagementActions() {
        val permissions = communityParticipantMenuPermissions(CommunityThreadRole.MEMBER, CommunityThreadRole.MEMBER, false)
        assertFalse(permissions.canTransfer)
        assertFalse(permissions.canChangeAdmin)
        assertFalse(permissions.canKick)
    }

    @Test fun ownerCanManageMembersAndAdminsButNotSelfOrOwner() {
        listOf(CommunityThreadRole.MEMBER, CommunityThreadRole.ADMIN).forEach { role ->
            val permissions = communityParticipantMenuPermissions(CommunityThreadRole.OWNER, role, false)
            assertTrue(permissions.canTransfer)
            assertTrue(permissions.canChangeAdmin)
            assertTrue(permissions.canKick)
        }
        listOf(
            communityParticipantMenuPermissions(CommunityThreadRole.OWNER, CommunityThreadRole.OWNER, false),
            communityParticipantMenuPermissions(CommunityThreadRole.OWNER, CommunityThreadRole.MEMBER, true),
        ).forEach { assertEquals(CommunityParticipantMenuPermissions(false, false, false), it) }
    }

    @Test fun adminCanKickButCannotChangeRoles() {
        val permissions = communityParticipantMenuPermissions(CommunityThreadRole.ADMIN, CommunityThreadRole.MEMBER, false)
        assertTrue(permissions.canKick)
        assertFalse(permissions.canChangeAdmin)
        assertFalse(permissions.canTransfer)
        assertFalse(communityParticipantMenuPermissions(CommunityThreadRole.ADMIN, CommunityThreadRole.OWNER, false).canKick)
    }
}
