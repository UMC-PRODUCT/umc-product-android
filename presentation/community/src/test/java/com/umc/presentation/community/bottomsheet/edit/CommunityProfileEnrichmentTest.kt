package com.umc.presentation.community.bottomsheet.edit

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CommunityProfileEnrichmentTest {
    @Test fun slowProfilesReturnTheOriginalListWithinOneBudget() = runTest {
        val members = (1..20).toList()
        val start = testScheduler.currentTime
        var active = 0
        var maximum = 0
        val result = enrichCommunityProfilesWithinBudget(members) {
            active++
            maximum = maxOf(maximum, active)
            try { delay(5_000); it * 10 } finally { active-- }
        }
        assertEquals(members, result)
        assertEquals(1_500L, testScheduler.currentTime - start)
        assertEquals(0, active)
        assertEquals(4, maximum)
    }

    @Test fun fastProfilesPreserveOrderAndCallerCancellationPropagates() = runTest {
        assertEquals(listOf(30, 10, 20), enrichCommunityProfilesWithinBudget(listOf(3, 1, 2)) { delay(10); it * 10 })
        var returned = false
        var cancelled = false
        val job = launch {
            enrichCommunityProfilesWithinBudget(listOf(1)) {
                try { awaitCancellation() } finally { cancelled = true }
            }
            returned = true
        }
        runCurrent()
        job.cancel()
        job.join()
        assertTrue(cancelled)
        assertTrue(!returned)
    }
}
