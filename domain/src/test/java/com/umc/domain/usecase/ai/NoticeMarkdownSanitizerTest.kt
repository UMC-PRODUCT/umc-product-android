package com.umc.domain.usecase.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NoticeMarkdownSanitizerTest {

    @Test
    fun `AI 결과의 앞뒤와 중간 빈 줄을 보존하고 개행만 통일한다`() {
        val input = "\r\n\r\n첫째\r\n\r\n\r\n둘째\r\n\r\n"
        assertEquals("\n\n첫째\n\n\n둘째\n\n", NoticeMarkdownSanitizer.sanitize(input))
        assertEquals("앞\n\n\n- 항목 · 내용\n\n\n뒤", NoticeMarkdownSanitizer.sanitize("앞\n\n\n| 항목 | 내용 |\n\n\n뒤"))
    }

    @Test
    fun `괄호형 번호만 표준 번호로 바꾸고 일반 문장 괄호는 유지한다`() {
        val input = "1) 첫째\n2) 둘째\n본문의 3) 표기는 유지\n4. 넷째"
        assertEquals(2, NoticeMarkdownSanitizer.countViolations(input))
        assertEquals("1. 첫째\n2. 둘째\n본문의 3) 표기는 유지\n4. 넷째", NoticeMarkdownSanitizer.sanitize(input))
    }

    @Test
    fun `AI 형광펜은 공식 기본 보라를 쓰고 코드와 기존 색상은 유지한다`() {
        val input = "<mark>강조</mark> `<mark>코드</mark>` <mark color=\"0,1,0,0.4\">기존</mark>"
        assertEquals(1, NoticeMarkdownSanitizer.countViolations(input))
        val sanitized = NoticeMarkdownSanitizer.sanitize(input)
        assertEquals(
            "<mark color=\"0.686,0.322,0.871,0.350\">강조</mark> `<mark>코드</mark>` <mark color=\"0,1,0,0.4\">기존</mark>",
            sanitized,
        )
        assertEquals(0, NoticeMarkdownSanitizer.countViolations(sanitized))
        assertEquals(0, NoticeMarkdownSanitizer.countViolations("\\<mark>리터럴</mark>"))
        assertEquals("\\<mark>리터럴</mark>", NoticeMarkdownSanitizer.sanitize("\\<mark>리터럴</mark>"))
        assertTrue(NOTICE_MARKDOWN_FORMAT_RULES.contains("0.000,0.478,1.000,0.350"))
    }

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
