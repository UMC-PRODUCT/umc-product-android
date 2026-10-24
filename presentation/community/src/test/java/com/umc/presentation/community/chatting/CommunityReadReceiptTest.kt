package com.umc.presentation.community.chatting

import org.junit.Assert.*
import org.junit.Test

class CommunityReadReceiptTest {
    private val ids = listOf("new", "visible", "old")

    @Test fun rejectedOrTimedOutReadCanRetryWithoutScrolling() {
        val receipt = CommunityReadReceipt().apply { observed = "visible" }
        receipt.queued("first", "visible")
        assertNull(receipt.candidate(ids))
        assertTrue(receipt.failed("first"))
        assertEquals("visible", receipt.candidate(ids))
        receipt.queued("retry", "visible")
        assertFalse(receipt.failed("first"))
        assertFalse(receipt.acknowledged("first", ids))
        assertEquals("retry" to "visible", receipt.pending)
        assertTrue(receipt.acknowledged("retry", ids))
        assertNull(receipt.candidate(ids))
    }

    @Test fun anotherDeviceConfirmationSupersedesPendingAndOldEvents() {
        val receipt = CommunityReadReceipt().apply { observed = "visible" }
        receipt.queued("pending", "visible")
        receipt.confirm("new", ids)
        assertNull(receipt.pending)
        receipt.confirm("old", ids)
        assertFalse(receipt.failed("pending"))
        assertNull(receipt.candidate(ids))
    }

    @Test fun incomingUnseenMessageDoesNotAdvanceVisibleBoundary() {
        val receipt = CommunityReadReceipt().apply { observed = "old" }
        receipt.queued("pending", "old")
        receipt.acknowledged("pending", ids)
        assertNull(receipt.candidate(listOf("unseen") + ids))
        receipt.observed = "visible"
        assertEquals("visible", receipt.candidate(ids))
    }

    @Test fun newerObservedBoundaryWaitsForPendingAcknowledgement() {
        val receipt = CommunityReadReceipt().apply { observed = "old" }
        receipt.queued("pending", "old")
        receipt.observed = "new"
        assertNull(receipt.candidate(ids))
        receipt.acknowledged("pending", ids)
        assertEquals("new", receipt.candidate(ids))
    }
}
