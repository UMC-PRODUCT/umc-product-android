package com.umc.domain.model.community.thread

import org.junit.Assert.*
import org.junit.Test

class CommunityMessageMergeTest {
    @Test fun `late edit cannot resurrect deletion`() {
        val deleted = CommunityThreadMessage(messageId = "1", status = "DELETED")
        assertEquals(deleted, CommunityMessageMerge.merge(deleted, deleted.copy(status = "SENT", content = "old")))
    }

    @Test fun `older edit does not overwrite latest content`() {
        val current = CommunityThreadMessage(messageId = "1", content = "new", editedAt = "2026-10-24T10:00:00Z")
        val old = current.copy(content = "old", editedAt = "2026-10-24T09:00:00Z")
        assertEquals(current, CommunityMessageMerge.merge(current, old))
        assertEquals(current, CommunityMessageMerge.merge(old, current))
    }
}
