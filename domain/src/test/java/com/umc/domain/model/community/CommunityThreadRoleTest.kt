package com.umc.domain.model.community

import org.junit.Assert.*
import org.junit.Test

class CommunityThreadRoleTest {
    @Test fun `owner can edit thread`() {
        assertTrue(CommunityThreadRole.OWNER.canEditThread())
    }

    @Test fun `admin can edit thread`() {
        assertTrue(CommunityThreadRole.ADMIN.canEditThread())
    }

    @Test fun `member cannot edit thread`() {
        assertFalse(CommunityThreadRole.MEMBER.canEditThread())
    }

    @Test fun `unknown role cannot edit thread`() {
        assertFalse(CommunityThreadRole.UNKNOWN.canEditThread())
    }
}
