package com.umc.presentation.notice.write

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownEditActionsTest {

    @Test
    fun `기울임 내부 식별자 밑줄은 편집 상태를 닫거나 다시 열지 않는다`() {
        val raw = "_user_name_"
        assertTrue(MarkdownStyle.ITALIC in MarkdownScanner.activeStyles(TextFieldValue(raw, TextRange(7))))
        assertTrue(MarkdownScanner.activeStyles(TextFieldValue(raw, TextRange(raw.length))).inline.isEmpty())
        assertTrue(MarkdownScanner.activeStyles(TextFieldValue("user_name", TextRange(9))).inline.isEmpty())
    }

    @Test
    fun `커서 뒤까지 이어지는 현재 줄 URL의 마커만 무시한다`() {
        val raw = "첫째\n[링크](https://a.dev/**path**) **강조**\n[다음](https://b.dev/_tail_)"
        val insideUrl = raw.indexOf("path")
        assertTrue(MarkdownScanner.activeStyles(TextFieldValue(raw, TextRange(insideUrl))).inline.isEmpty())
        val insideBold = raw.indexOf("강조") + 1
        assertEquals(setOf(MarkdownStyle.BOLD), MarkdownScanner.activeStyles(TextFieldValue(raw, TextRange(insideBold))).inline)
    }

    @Test
    fun `여는 마커 직후 Enter는 본문 서식을 다음 줄로 보존하고 공백만 있는 미완성 마커는 걷어낸다`() {
        listOf(
            Triple("**가나**", 2, "\n**가나**"),
            Triple("**   ", 5, "   \n"),
            Triple("<u>**가나**</u>", 5, "\n<u>**가나**</u>"),
            Triple("**_   ", 6, "   \n"),
        ).forEach { (raw, at, expected) ->
            val result = MarkdownEditActions.processContentChange(
                TextFieldValue(raw, TextRange(at)),
                TextFieldValue(raw.replaceRange(at, at, "\n"), TextRange(at + 1)),
            )
            assertEquals(raw, expected, result.text)
            assertEquals(TextRange(expected.indexOf('\n') + 1), result.selection)
        }
    }

    @Test
    fun `일반 Enter는 composition을 포함한 현재 값을 그대로 반환한다`() {
        val previous = TextFieldValue("일반 본문", TextRange(5))
        val current = TextFieldValue("일반 본문\n", TextRange(6), composition = TextRange(0, 5))
        assertEquals(current, MarkdownEditActions.processContentChange(previous, current))
    }

    @Test
    fun `링크 URL의 보이는 백슬래시는 유지하고 코드의 숨겨진 백슬래시는 함께 삭제한다`() {
        val link = """[링크](https://a.com/a\(1\))"""
        val deletedAt = link.indexOf(')')
        val after = TextFieldValue(link.removeRange(deletedAt, deletedAt + 1), TextRange(deletedAt))
        assertEquals(after, MarkdownEditActions.processContentChange(
            TextFieldValue(link, TextRange(deletedAt + 1)), after,
        ))
        val code = MarkdownEditActions.processContentChange(
            TextFieldValue("`\\a`", TextRange(3)), TextFieldValue("`\\`", TextRange(2)),
        )
        assertEquals("``", code.text)
        assertEquals(TextRange(1), code.selection)
    }

    @Test
    fun `길이만 바뀐 치환은 단일 삽입 삭제로 보정하지 않는다`() {
        val replaced = TextFieldValue("\\xb", TextRange(1))
        assertEquals(replaced, MarkdownEditActions.processContentChange(
            TextFieldValue("\\*ab", TextRange(2)), replaced,
        ))
        val inserted = TextFieldValue("* ", TextRange(2))
        assertEquals(inserted, MarkdownEditActions.processContentChange(
            TextFieldValue("x", TextRange(1)), inserted,
        ))
        val newline = TextFieldValue("- b\n", TextRange(4))
        assertEquals(newline, MarkdownEditActions.processContentChange(
            TextFieldValue("- a", TextRange(3)), newline,
        ))
    }

    @Test
    fun `서식 구간 중간을 개행하면 뒤쪽 닫는 마커를 제거한다`() {
        val previous = "- **abcd**"
        val result = MarkdownEditActions.processContentChange(
            TextFieldValue(previous, TextRange(6)), TextFieldValue("- **ab\ncd**", TextRange(7)),
        )
        assertEquals("- **ab**\n- cd", result.text)
        assertEquals(TextRange(11), result.selection)
        val nested = "- <u>**abcd**</u>"
        val at = nested.indexOf("cd")
        val split = MarkdownEditActions.processContentChange(
            TextFieldValue(nested, TextRange(at)),
            TextFieldValue(nested.replaceRange(at, at, "\n"), TextRange(at + 1)),
        )
        assertEquals("- <u>**ab**</u>\n- cd", split.text)
    }

    @Test
    fun `끝 공백은 닫는 서식 밖으로 이동하고 코드 내부 공백은 유지한다`() {
        listOf(
            "- **강조 " to "- **강조** \n- ",
            "- `코드 " to "- `코드 `\n- ",
        ).forEach { (raw, expected) ->
            val result = MarkdownEditActions.processContentChange(
                TextFieldValue(raw, TextRange(raw.length)),
                TextFieldValue(raw + "\n", TextRange(raw.length + 1)),
            )
            assertEquals(expected, result.text)
        }
    }

    @Test
    fun `Int 경계 번호 목록은 Long으로 증가 재번호하고 Long 상한에서는 오버플로 없이 증가를 생략한다`() {
        val raw = "2147483647. 항목\n2147483648. 다음"
        val at = raw.indexOf('\n')
        val result = MarkdownEditActions.processContentChange(
            TextFieldValue(raw, TextRange(at)),
            TextFieldValue(raw.replaceRange(at, at, "\n"), TextRange(at + 1)),
        )
        assertEquals("2147483647. 항목\n2147483648. \n2147483649. 다음", result.text)
        val maxLong = "9223372036854775807. 항목"
        assertEquals("$maxLong\n", MarkdownEditActions.processContentChange(
            TextFieldValue(maxLong, TextRange(maxLong.length)),
            TextFieldValue(maxLong + "\n", TextRange(maxLong.length + 1)),
        ).text)
    }

    @Test
    fun `공백만 선택해 서식을 적용하면 원문과 선택 범위를 보존한다`() {
        val value = TextFieldValue("   ", TextRange(0, 3))
        listOf(
            MarkdownEditActions.toggleBold(value),
            MarkdownEditActions.toggleItalic(value),
            MarkdownEditActions.toggleUnderline(value),
            MarkdownEditActions.toggleStrikethrough(value),
        ).forEach { assertEquals(value, it) }
    }

    @Test
    fun `코드 붙여넣기는 내부 레거시 마커의 공백을 바꾸지 않는다`() {
        val raw = "`** a ** ~~ x ~~` **일시: **"
        val result = MarkdownEditActions.processContentChange(
            TextFieldValue(), TextFieldValue(raw, TextRange(raw.length)),
        )
        assertEquals("`** a ** ~~ x ~~` **일시:** ", result.text)
        val codeRaw = "**`<u>코드"
        val code = TextFieldValue(codeRaw, TextRange(codeRaw.length))
        assertEquals(setOf(MarkdownStyle.CODE), MarkdownScanner.activeStyles(code).inline)
    }

    @Test
    fun `글머리와 대시 목록에서 Enter를 치면 다음 항목을 자동 생성하고 빈 줄이면 탈출한다`() {
        val prevBullet = TextFieldValue("- 항목1", TextRange(5))
        val curBullet = TextFieldValue("- 항목1\n", TextRange(6))
        val nextBullet = MarkdownEditActions.processContentChange(prevBullet, curBullet)
        assertEquals("- 항목1\n- ", nextBullet.text)
        assertEquals(TextRange(8), nextBullet.selection)

        val prevEmptyDash = TextFieldValue("– ", TextRange(2))
        val curEmptyDash = TextFieldValue("– \n", TextRange(3))
        val exitedDash = MarkdownEditActions.processContentChange(prevEmptyDash, curEmptyDash)
        assertEquals("", exitedDash.text)
        assertEquals(TextRange(0), exitedDash.selection)
    }

    @Test
    fun `번호 목록 중간에서 Enter를 치면 번호가 증가하고 후속 항목이 자동 재정렬된다`() {
        val prev = TextFieldValue("1. 첫째\n2. 둘째\n3. 셋째", TextRange(5))
        val cur = TextFieldValue("1. 첫째\n\n2. 둘째\n3. 셋째", TextRange(6))

        val result = MarkdownEditActions.processContentChange(prev, cur)
        assertEquals("1. 첫째\n2. \n3. 둘째\n4. 셋째", result.text)
        assertEquals(TextRange(9), result.selection)
    }

    @Test
    fun `목록 줄에서 열린 인라인 마커가 있으면 줄 끝에서 닫고 다음 목록 항목을 시작한다`() {
        val prev = TextFieldValue("- **강조", TextRange(6))
        val cur = TextFieldValue("- **강조\n", TextRange(7))

        val result = MarkdownEditActions.processContentChange(prev, cur)
        assertEquals("- **강조**\n- ", result.text)
        assertEquals(TextRange(11), result.selection)
    }

    @Test
    fun `줄 시작에서 별표 공백을 입력하면 글머리 기호로 자동 변환된다`() {
        val prev = TextFieldValue("*", TextRange(1))
        val cur = TextFieldValue("* ", TextRange(2))

        val result = MarkdownEditActions.processContentChange(prev, cur)
        assertEquals("- ", result.text)
        assertEquals(TextRange(2), result.selection)
    }

    @Test
    fun `닫는 인라인 마커 완성 시 composition을 종료해 서식 상속을 방지한다`() {
        val prev = TextFieldValue("`code", TextRange(5), composition = TextRange(1, 5))
        val cur = TextFieldValue("`code`", TextRange(6), composition = TextRange(1, 6))

        val result = MarkdownEditActions.processContentChange(prev, cur)
        assertEquals("`code`", result.text)
        assertNull(result.composition)
    }

    @Test
    fun `붙여넣기 시 CRLF·ZWS·불릿 기호·레거시 공백 마커를 정규화한다`() {
        val prev = TextFieldValue("", TextRange(0))
        val pasted = "• 항목1\r\n• **일시: **\u200B"
        val cur = TextFieldValue(pasted, TextRange(pasted.length))

        val result = MarkdownEditActions.processContentChange(prev, cur)
        assertEquals("- 항목1\n- **일시:** ", result.text)
        assertEquals(TextRange(result.text.length), result.selection)
    }

    @Test
    fun `숨겨진 이스케이프 대상 문자를 백스페이스로 지우면 앞의 백슬래시도 함께 제거된다`() {
        val prev = TextFieldValue("""\*중요""", TextRange(2))
        val cur = TextFieldValue("""\중요""", TextRange(1))

        val result = MarkdownEditActions.processContentChange(prev, cur)
        assertEquals("중요", result.text)
        assertEquals(TextRange(0), result.selection)

        // 일반 문자 앞의 백슬래시(C:\a)는 숨겨진 이스케이프가 아니므로 유지한다
        val prevPath = TextFieldValue("""C:\a""", TextRange(4))
        val curPath = TextFieldValue("""C:\""", TextRange(3))
        val pathResult = MarkdownEditActions.processContentChange(prevPath, curPath)
        assertEquals("""C:\""", pathResult.text)
    }

    @Test
    fun `번호 목록 줄에서 제목이나 인용구를 적용하면 기존 번호 prefix가 깔끔히 교체된다`() {
        val numbered = TextFieldValue("1. 항목", TextRange(5))
        val heading = MarkdownEditActions.applyHeading(numbered, MarkdownHeading.TITLE2)
        assertEquals("## 항목", heading.text)

        val quoted = MarkdownEditActions.toggleQuote(numbered)
        assertEquals("> 항목", quoted.text)
    }

    @Test
    fun `MarkdownScanner는 이스케이프된 마커를 무시하고 대시·번호 목록 상태를 판별한다`() {
        val escaped = TextFieldValue("""\*기울임아님""", TextRange(7))
        assertFalse(MarkdownStyle.ITALIC in MarkdownScanner.activeStyles(escaped))

        val dash = TextFieldValue("– 대시항목", TextRange(6))
        assertTrue(MarkdownScanner.activeStyles(dash).isDashList)

        val number = TextFieldValue("12. 번호항목", TextRange(8))
        assertTrue(MarkdownScanner.activeStyles(number).isNumberList)
    }
}
