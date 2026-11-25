package com.umc.presentation.notice

import org.junit.Assert.assertEquals
import org.junit.Test

class NoticeMarkdownPlainTextTest {

    @Test
    fun `공지 목록 미리보기 평문 추출이 블록과 인라인 마커 및 링크 URL을 제거한다`() {
        val raw = """
            # [필독] 12기 안내
            > **중요** 공지입니다.
            - <mark color="0.686,0.322,0.871,0.350">형광펜</mark> 및 [링크](https://umc.makeus.in)
            1. `코드`와 ~~취소선~~
        """.trimIndent()

        val plain = extractNoticePlainText(raw)

        assertEquals(
            """
                [필독] 12기 안내
                중요 공지입니다.
                • 형광펜 및 링크
                1. 코드와 취소선
            """.trimIndent(),
            plain,
        )
    }

    @Test
    fun `2패스 파싱으로 이스케이프된 마커와 레거시 공백 마커까지 깔끔하게 제거한다`() {
        val raw = """\*\*굿\*\* **일시: ** \u200B""".replace("""\u200B""", "\u200B")
        val plain = extractNoticePlainText(raw)
        assertEquals("굿 일시:", plain)
    }

    @Test
    fun `빈 문자열이나 공백만 있으면 빈 문자열을 반환한다`() {
        assertEquals("", extractNoticePlainText("   \r\n  "))
    }
}
