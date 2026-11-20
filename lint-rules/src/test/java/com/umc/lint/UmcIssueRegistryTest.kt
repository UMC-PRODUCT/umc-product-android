package com.umc.lint

import com.android.tools.lint.client.api.LintClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class UmcIssueRegistryTest {

    private val issues = run {
        // Issue 를 처음 만들 때 lint 가 clientName 을 읽는다. 다른 lint 테스트보다 먼저 돌아도 실패하지 않게 직접 정한다.
        LintClient.clientName = LintClient.CLIENT_UNIT_TESTS
        UmcIssueRegistry().issues
    }

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

    /**
     * CodeRabbit 은 `.coderabbit.yaml` 의 자동 검사 목록에 있는 규칙을 지적하지 않는다.
     * 규칙을 추가·삭제하거나 심각도를 바꾸고 목록을 그대로 두면, 리뷰가 같은 내용을 또 지적하거나 놓친다.
     */
    @Test
    fun `CodeRabbit 자동 검사 목록이 레지스트리와 같다`() {
        val root = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
            .first { File(it, CODERABBIT_CONFIG).exists() }
        val listed = AUTO_CHECK_LINE.findAll(File(root, CODERABBIT_CONFIG).readText())
            .map { it.groupValues[1] to it.groupValues[2] }
            .toList()
        val registered = issues.map { it.id to it.defaultSeverity.name }

        assertEquals("$CODERABBIT_CONFIG 목록에 같은 규칙이 두 번 있다", listed.size, listed.toSet().size)
        assertEquals(
            "$CODERABBIT_CONFIG 의 자동 검사 목록(\"- 규칙ID (심각도): 설명\")을 레지스트리에 맞추세요",
            registered.sortedBy { it.first },
            listed.sortedBy { it.first },
        )
    }

    private companion object {
        const val CODERABBIT_CONFIG = ".coderabbit.yaml"

        /** `          - UseUText (ERROR): …` 형식의 줄. 심각도가 있는 줄만 규칙 항목으로 본다. */
        val AUTO_CHECK_LINE = Regex("""(?m)^\s+- ([A-Za-z]+) \((ERROR|WARNING|FATAL|INFORMATIONAL)\):""")
    }
}
