package com.umc.presentation.notice.write

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownParserTest {
    @Test
    fun `중첩 토큰은 부모 본문 안의 절대 원문 범위를 반환한다`() {
        val raw = "앞 ***a* b** 뒤"
        val parent = MarkdownParser.parseInline(raw).single()
        assertEquals(InlineTokenKind.BOLD, parent.kind)
        assertEquals("**", raw.substring(parent.start, parent.innerStart))
        assertEquals("**", raw.substring(parent.innerEnd, parent.end))
        val child = parent.children.single()
        assertEquals(InlineTokenKind.ITALIC_ASTERISK, child.kind)
        assertEquals("a", raw.substring(child.innerStart, child.innerEnd))
        assertTrue(child.start >= parent.innerStart && child.end <= parent.innerEnd)
    }

    @Test
    fun `식별자 안 밑줄도 문서 정규식의 첫 유효한 짝으로 닫는다`() {
        val regex = Regex("""(?<!\\)(?<!_)_(?=\S)(.+?)(?<=\S)_(?!_)""")
        listOf("prefix_mid_suffix", "_user_name_", "_italic_", "user_name").forEach { raw ->
            val expected = regex.find(raw)
            val actual = MarkdownParser.parseInline(raw, includeEmptyPairs = false).firstOrNull()
            assertEquals(raw, expected?.range?.first, actual?.start)
            assertEquals(raw, expected?.range?.last?.plus(1), actual?.end)
        }
        assertEquals("prefixmidsuffix", MarkdownParser.plainText("prefix_mid_suffix"))
        assertEquals("username_", MarkdownParser.plainText("_user_name_"))
    }

    @Test
    fun `코드와 링크 URL 내부는 토큰으로 다시 해석하지 않는다`() {
        val code = MarkdownParser.parseInline("`**a** <u>b</u>`").single()
        assertEquals(InlineTokenKind.CODE, code.kind)
        assertTrue(code.children.isEmpty())
        val link = MarkdownParser.parseInline("[**a**](https://a.dev/_b_)").single()
        assertEquals(listOf(InlineTokenKind.BOLD), link.children.map { it.kind })
    }

    @Test
    fun `공백과 미완성 입력은 읽기 토큰으로 확정하지 않는다`() {
        listOf("** a **", "*** ***", "**_ a _**", "**a", "_a").forEach { raw ->
            assertTrue(raw, MarkdownParser.parseInline(raw, includeEmptyPairs = false).isEmpty())
        }
        assertTrue(MarkdownParser.parseInline("<u></u>").single().kind.isEmptyPair)
        assertTrue(MarkdownParser.parseInline("<u></u>", includeEmptyPairs = false).isEmpty())
    }

    @Test
    fun `숨김 escape는 일반 코드 URL 문맥을 구분한다`() {
        assertTrue(MarkdownParser.isHiddenEscapeAt("\\*a", 0))
        assertFalse(MarkdownParser.isHiddenEscapeAt("C:\\Users", 2))
        assertTrue(MarkdownParser.isHiddenEscapeAt("`\\a`", 1))
        val url = "[a](https://a.dev/\\))"
        assertFalse(MarkdownParser.isHiddenEscapeAt(url, url.indexOf('\\')))
    }
}
