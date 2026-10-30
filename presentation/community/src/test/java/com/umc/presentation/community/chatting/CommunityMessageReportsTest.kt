package com.umc.presentation.community.chatting

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.FailState
import com.umc.domain.model.community.thread.CommunityMessageReportReason
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class CommunityMessageReportsTest {
    @Test fun eachSelectedReasonUsesOneSingleReasonRequestInDisplayOrder() = runBlocking {
        val visited = mutableListOf<CommunityMessageReportReason>()
        val result = submitCommunityMessageReports(setOf(CommunityMessageReportReason.PRIVACY, CommunityMessageReportReason.SPAM)) {
            visited += it
            ApiState.Success(Unit)
        }
        assertEquals(listOf(CommunityMessageReportReason.SPAM, CommunityMessageReportReason.PRIVACY), visited)
        assertEquals(visited.toSet(), result.accepted)
        assertTrue(result.failures.isEmpty())
    }

    @Test fun duplicateRejectionDoesNotHidePartialSuccessOrSkipRemainingReasons() = runBlocking {
        val visited = mutableListOf<CommunityMessageReportReason>()
        val reasons = CommunityMessageReportReason.entries.toSet()
        val duplicate = FailState(code = "REPORT_ALREADY_EXISTS", message = "duplicate")
        val result = submitCommunityMessageReports(reasons) {
            visited += it
            if (it == CommunityMessageReportReason.SPAM) ApiState.Success(Unit) else ApiState.Fail(duplicate)
        }
        assertEquals(reasons.toList(), visited)
        assertEquals(setOf(CommunityMessageReportReason.SPAM), result.accepted)
        assertEquals(reasons - CommunityMessageReportReason.SPAM, result.failures.keys)
    }

    @Test fun emptySelectionSendsNothingAndCancellationStopsFurtherRequests() = runBlocking {
        val visited = mutableListOf<CommunityMessageReportReason>()
        val empty = submitCommunityMessageReports(emptySet()) { fail("No request expected"); ApiState.Success(Unit) }
        assertTrue(empty.accepted.isEmpty())
        try {
            submitCommunityMessageReports(CommunityMessageReportReason.entries.toSet()) {
                visited += it
                throw CancellationException()
            }
            fail("Cancellation must propagate")
        } catch (_: CancellationException) {
            assertEquals(listOf(CommunityMessageReportReason.SPAM), visited)
        }
    }
}
