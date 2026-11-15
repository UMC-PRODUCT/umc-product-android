package com.umc.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserInfoEntryTest {
    private fun role(challengerId: Long) = UserRole(
        id = 0L, challengerId = challengerId, roleType = "CHALLENGER", chapterId = null, chapterName = null,
        organizationType = "SCHOOL", organizationId = null, responsiblePart = null, gisuId = 1L, gisu = 1L,
    )

    private fun record(challengerId: Long) = ChallengerRecord(
        challengerId = challengerId, memberId = 1L, gisuId = 1L, gisu = 1L, chapterId = null, chapterName = null,
        part = null, challengerStatus = null, totalPoint = null, name = "", nickname = "", email = "",
        schoolId = 0L, schoolName = "", profileImageLink = "", status = "ACTIVE",
    )

    @Test fun `no challenger record goes to code input`() {
        assertFalse(UserInfo().canEnterMain())
        assertFalse(UserInfo(roles = listOf(role(0L)), challengerRecords = listOf(record(0L))).canEnterMain())
    }

    @Test fun `challenger id in roles or records enters main`() {
        assertTrue(UserInfo(roles = listOf(role(10L))).canEnterMain())
        assertTrue(UserInfo(challengerRecords = listOf(record(10L))).canEnterMain())
    }

    @Test fun `umc product account enters main without challenger record`() {
        assertTrue(UserInfo(isUmcProduct = true).canEnterMain())
    }
}
