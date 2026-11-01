package com.example.mypage.memberprofile

import kotlinx.collections.immutable.persistentSetOf
import org.junit.Assert.*
import org.junit.Test

class MemberReportValidationTest {
    private val valid = MemberProfileUiState(reportReasons = persistentSetOf(MemberReportReason.SPAM, MemberReportReason.HARASSMENT), reportDetail = "신고 내용")

    @Test fun acceptsMultipleReasonsAndExactly500Characters() {
        assertTrue(valid.canSubmitReport)
        assertTrue(valid.copy(reportDetail = "a".repeat(500)).canSubmitReport)
        assertFalse(valid.copy(reportDetail = "a".repeat(501)).canSubmitReport)
    }

    @Test fun preventsEmptySelfAndConcurrentReports() {
        assertFalse(valid.copy(reportReasons = persistentSetOf()).canSubmitReport)
        assertFalse(valid.copy(reportDetail = " \n\t").canSubmitReport)
        assertFalse(valid.copy(isMine = true).canSubmitReport)
        assertFalse(valid.copy(isProcessing = true).canSubmitReport)
    }
}
