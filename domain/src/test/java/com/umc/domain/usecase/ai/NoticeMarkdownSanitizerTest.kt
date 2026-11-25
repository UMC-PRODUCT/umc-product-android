package com.umc.domain.usecase.ai

import org.junit.Assert.assertEquals
import org.junit.Test

class NoticeMarkdownSanitizerTest {

    @Test
    fun `번호 목록은 글머리 기호로 강제 치환하지 않고 보존한다`() {
        val input = """
            # 회의 안내
            1. 첫 번째 안건
            2. 두 번째 안건
            – 대시 항목
            - 글머리 항목
        """.trimIndent()

        assertEquals(0, NoticeMarkdownSanitizer.countViolations(input))
        assertEquals(input, NoticeMarkdownSanitizer.sanitize(input))
    }

    @Test
    fun `표·수평선·코드펜스·체크박스 보정은 기존대로 유지한다`() {
        val input = """
            ```markdown
            다음은 요약입니다.
            #제목
            - [x] 완료 항목
            ---
            | 항목 | 내용 |
            | --- | --- |
            | 일시 | 토요일 |
            ```
        """.trimIndent()

        val expected = """
            # 제목
            - 완료 항목
            - 항목 · 내용
            - 일시 · 토요일
        """.trimIndent()

        assertEquals(expected, NoticeMarkdownSanitizer.sanitize(input))
    }
}
