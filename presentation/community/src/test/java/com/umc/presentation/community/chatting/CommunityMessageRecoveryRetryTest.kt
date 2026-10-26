package com.umc.presentation.community.chatting

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class CommunityMessageRecoveryRetryTest {
    @Test fun `transient failures recover without another socket event`() = runBlocking {
        val waits = mutableListOf<Long>()
        var attempts = 0
        assertTrue(recoverMessagesWithRetry(wait = { waits.add(it) }) { ++attempts == 3 })
        assertEquals(3, attempts)
        assertEquals(listOf(1_000L, 2_000L), waits)
    }

    @Test fun `persistent failure has bounded attempts`() = runBlocking {
        var attempts = 0
        val waits = mutableListOf<Long>()
        assertFalse(recoverMessagesWithRetry(wait = { waits.add(it) }) { attempts++; false })
        assertEquals(3, attempts)
        assertEquals(listOf(1_000L, 2_000L), waits)
    }

    @Test fun `success or terminal access outcome stops immediately`() = runBlocking {
        var attempts = 0
        assertTrue(recoverMessagesWithRetry(wait = { fail("Unexpected retry") }) { attempts++; true })
        assertEquals(1, attempts)
    }

    @Test fun `cancellation during backoff stops recovery`() = runBlocking {
        var attempts = 0
        try {
            recoverMessagesWithRetry(wait = { throw CancellationException("Screen closed") }) {
                attempts++
                false
            }
            fail("Cancellation must propagate")
        } catch (_: CancellationException) {
            assertEquals(1, attempts)
        }
    }
}
