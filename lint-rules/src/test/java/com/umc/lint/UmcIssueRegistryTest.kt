package com.umc.lint

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UmcIssueRegistryTest {

    private val issues = UmcIssueRegistry().issues

    /** CI 게이트는 category 값으로 커스텀 규칙을 고른다. 다른 카테고리를 쓰면 게이트에서 빠진다. */
    @Test
    fun `모든 규칙이 UMC 카테고리를 쓴다`() {
        val outside = issues.filter { it.category != UMC_CATEGORY }.map { it.id }
        assertTrue("UMC_CATEGORY를 쓰지 않는 규칙: $outside", outside.isEmpty())
    }

    @Test
    fun `규칙 ID가 겹치지 않는다`() {
        assertEquals(issues.size, issues.map { it.id }.toSet().size)
    }
}
