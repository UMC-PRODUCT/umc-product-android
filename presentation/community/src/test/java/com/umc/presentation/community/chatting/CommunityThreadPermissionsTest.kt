package com.umc.presentation.community.chatting

import com.umc.domain.model.community.thread.CommunityThreadRole
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CommunityThreadPermissionsTest {
    @Test fun ownerAndAdminCanOpenAndExecuteThreadManagement() {
        assertTrue(CommunityThreadRole.OWNER.canManageCommunityThread())
        assertTrue(CommunityThreadRole.ADMIN.canManageCommunityThread())
    }

    @Test fun memberUnknownAndUnloadedThreadCannotManageTheThread() {
        assertFalse(CommunityThreadRole.MEMBER.canManageCommunityThread())
        assertFalse(CommunityThreadRole.UNKNOWN.canManageCommunityThread())
        assertFalse(null.canManageCommunityThread())
    }
}
