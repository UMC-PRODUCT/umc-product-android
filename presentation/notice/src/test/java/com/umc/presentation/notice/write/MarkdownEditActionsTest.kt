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
    fun `표시 텍스트 재선택으로 중첩 밑줄과 같은 형광펜을 해제한다`() {
        val underline = "<u>**word**</u>"
        val highlight = "<mark color=\"${MarkdownHighlightColor.PURPLE.markColorCode}\">**word**</mark>"
        assertEquals("**word**", MarkdownEditActions.toggleUnderline(displaySelection(underline, 0, 4)).text)
        assertEquals("**word**", MarkdownEditActions.toggleHighlight(displaySelection(highlight, 0, 4), MarkdownHighlightColor.PURPLE).text)
        assertEquals("**word**", MarkdownEditActions.toggleStrikethrough(displaySelection("~~**word**~~", 0, 4)).text)
    }

    @Test
    fun `부분 선택 해제는 양옆 본문의 기존 서식을 보존한다`() {
        val result = MarkdownEditActions.toggleUnderline(displaySelection("<u>**word**</u>", 1, 3))
        assertEquals("<u>**w**</u>**or**<u>**d**</u>", result.text)
        assertEquals("word", editorText(result.text))
        assertEquals("or", result.text.substring(result.selection.min, result.selection.max))
    }

    @Test
    fun `부분 형광펜 색 변경과 새 밑줄 적용은 표준 래핑 순서를 따른다`() {
        val purple = "<mark color=\"${MarkdownHighlightColor.PURPLE.markColorCode}\">"
        val pink = "<mark color=\"${MarkdownHighlightColor.PINK.markColorCode}\">"
        val result = MarkdownEditActions.toggleHighlight(displaySelection(purple + "**word**</mark>", 1, 3), MarkdownHighlightColor.PINK)
        assertEquals(purple + "**w**</mark>" + pink + "**or**</mark>" + purple + "**d**</mark>", result.text)
        assertEquals("<u>**word**</u>", MarkdownEditActions.toggleUnderline(displaySelection("**word**", 0, 4)).text)
    }

    @Test
    fun `선택 토글은 코드와 링크 URL 및 이스케이프를 보존한다`() {
        val code = displaySelection("`<u>word</u>`", 3, 7)
        assertEquals(code, MarkdownEditActions.toggleUnderline(code))
        val link = "[**word**](https://a.dev/_path_)"
        assertEquals("[<u>**word**</u>](https://a.dev/_path_)", MarkdownEditActions.toggleUnderline(displaySelection(link, 1, 5)).text)
        val escaped = MarkdownEditActions.toggleUnderline(displaySelection("\\*literal", 0, 1))
        assertEquals("<u>\\*</u>literal", escaped.text)
        assertEquals("*literal", editorText(escaped.text))
        val beforeEscape = MarkdownEditActions.toggleUnderline(displaySelection("a\\*", 0, 1))
        assertEquals("<u>a</u>\\*", beforeEscape.text)
        assertEquals("a*", editorText(beforeEscape.text))
    }

    @Test
    fun `공백 경계와 여러 줄 선택은 블록과 유효한 인라인 마커를 유지한다`() {
        val result = MarkdownEditActions.toggleUnderline(displaySelection("**a b**", 0, 1))
        assertEquals("<u>**a**</u> **b**", result.text)
        val raw = "# **one**\n- **two**"
        val value = TextFieldValue(raw, TextRange(0, raw.length))
        assertEquals("# <u>**one**</u>\n- <u>**two**</u>", MarkdownEditActions.toggleUnderline(value).text)
    }

    @Test
    fun `원문 3000자 이내의 여러 줄 본문에서 붙여넣기와 인라인 분석 결과를 보존한다`() {
        val raw = List(180) { "**body** `code`" }.joinToString("\n").padEnd(2998, 'x')
        val before = TextFieldValue(raw, TextRange(raw.length))
        val after = TextFieldValue(raw + "xy", TextRange(raw.length + 2))
        assertEquals(3000, after.text.length)
        assertEquals(after, MarkdownEditActions.processContentChange(before, after))

        val pasted = raw.replace("\n", "\r\n")
        val normalized = MarkdownEditActions.processContentChange(
            TextFieldValue(), TextFieldValue(pasted, TextRange(pasted.length)),
        )
        assertEquals(raw, normalized.text)
        assertEquals(TextRange(raw.length), normalized.selection)

        val tokens = MarkdownParser.parseInline(after.text)
        assertEquals(180, tokens.count { it.kind == InlineTokenKind.BOLD })
        assertEquals(180, tokens.count { it.kind == InlineTokenKind.CODE })
        assertEquals(List(180) { "body code" }.joinToString("\n") + "x".repeat(119) + "xy",
            MarkdownParser.plainText(after.text))
    }

    @Test
    fun `제목은 목록 인용 활성 표시와 겹치지 않고 태그 닫힘은 조합을 종료한다`() {
        listOf("# - title", "## > title").forEach { raw ->
            val state = MarkdownScanner.activeStyles(TextFieldValue(raw, TextRange(raw.length)))
            assertFalse(state.isBullet)
            assertFalse(state.isQuote)
        }
        val raw = "<u>x</u>"
        val result = MarkdownEditActions.processContentChange(
            TextFieldValue(raw.dropLast(1), TextRange(raw.length - 1)),
            TextFieldValue(raw, TextRange(raw.length), composition = TextRange(3, raw.length)),
        )
        assertNull(result.composition)
    }

    @Test
    fun `선택 안팎의 연속 조상 서식은 내부 공백에서도 유지한다`() {
        val mark = "<mark color=\"${MarkdownHighlightColor.PURPLE.markColorCode}\">"
        assertEquals(mark + "<u>안녕</u> 하세요</mark>",
            MarkdownEditActions.toggleUnderline(displaySelection(mark + "안녕 하세요</mark>", 0, 2)).text)
        val raw = mark + "앞 **굵게** 뒤</mark> 끝"
        assertEquals(mark + "앞 **굵게** 뒤</mark> <u>끝</u>",
            MarkdownEditActions.toggleUnderline(displaySelection(raw, 7, 8)).text)
        assertEquals("**앞** <u>**가**</u> **뒤**",
            MarkdownEditActions.toggleUnderline(displaySelection("**앞 가 뒤**", 2, 3)).text)
        assertEquals("~~앞 <u>가</u> 뒤~~",
            MarkdownEditActions.toggleUnderline(displaySelection("~~앞 가 뒤~~", 2, 3)).text)
    }

    @Test
    fun `리터럴 백슬래시 뒤에 새 경계 마커를 넣어도 표시와 선택을 보존한다`() {
        val raw = "a\\ b"
        val selection = TextFieldValue(raw, TextRange(0, 2))
        val strike = MarkdownEditActions.toggleStrikethrough(selection)
        assertEquals("~~a\\\\~~ b", strike.text)
        assertEquals(raw, editorText(strike.text))
        assertEquals("a\\\\", strike.text.substring(strike.selection.min, strike.selection.max))
        val underline = MarkdownEditActions.toggleUnderline(selection)
        assertEquals("<u>a\\\\</u> b", underline.text)
        assertEquals(raw, editorText(underline.text))
        val finalSlash = MarkdownEditActions.toggleUnderline(TextFieldValue("a\\", TextRange(0, 2)))
        assertEquals("a\\\\", finalSlash.text.substring(finalSlash.selection.min, finalSlash.selection.max))
        val reversed = MarkdownEditActions.toggleStrikethrough(selection.copy(selection = TextRange(2, 0)))
        assertEquals(strike.text, reversed.text)
        assertTrue(reversed.selection.start > reversed.selection.end)
    }

    private fun editorText(raw: String): String = MarkdownVisualTransformation(
        androidx.compose.ui.graphics.Color.Unspecified, androidx.compose.ui.graphics.Color.Unspecified,
    ).parse(raw).transformed.text.text

    private fun displaySelection(raw: String, start: Int, end: Int): TextFieldValue {
        val mapping = MarkdownVisualTransformation(
            androidx.compose.ui.graphics.Color.Unspecified, androidx.compose.ui.graphics.Color.Unspecified,
        ).parse(raw).transformed.offsetMapping
        return TextFieldValue(raw, TextRange(mapping.transformedToOriginal(start), mapping.transformedToOriginal(end)))
    }

    @Test
    fun `코드를 닫는 붙여넣기는 코드 원문과 이후 일반 서식을 구분한다`() {
        val raw = "`prefix "
        val at = raw.length
        val inserted = "** a **` ** b **"
        val result = MarkdownEditActions.processContentChange(
            TextFieldValue(raw, TextRange(at)),
            TextFieldValue(raw + inserted, TextRange(at + inserted.length)),
        )
        assertEquals("`prefix ** a **`  **b** ", result.text)
        assertEquals(TextRange(result.text.length), result.selection)
    }

    @Test
    fun `선택 치환과 미완성 코드 안 붙여넣기도 원문을 보존한다`() {
        listOf("`old`" to TextRange(1, 4), "`" to TextRange(1)).forEach { (raw, selection) ->
            val inserted = "~~ x ~~"
            val current = TextFieldValue(raw.replaceRange(selection.min, selection.max, inserted), TextRange(selection.min + inserted.length))
            assertEquals(current, MarkdownEditActions.processContentChange(TextFieldValue(raw, selection), current))
        }
    }

    @Test
    fun `중첩 서식의 시작 중간 끝과 목록 Enter는 본문과 토큰 경계를 보존한다`() {
        listOf(
            Triple("***a* b**", 4, "***a***\n b"),
            Triple("***a* b**", 9, "***a* b**\n"),
            Triple("- ***a* b**", 5, "- \n- ***a* b**"),
            Triple("**_abc_**", 3, "\n**_abc_**"),
            Triple("<mark><u>abc</u></mark>", 9, "\n<mark><u>abc</u></mark>"),
            Triple("`abc`", 1, "\n`abc`"),
            Triple("`abc`", 3, "`ab`\nc"),
            Triple("****", 2, "\n"),
        ).forEach { (raw, at, expected) ->
            val result = MarkdownEditActions.processContentChange(
                TextFieldValue(raw, TextRange(at)),
                TextFieldValue(raw.replaceRange(at, at, "\n"), TextRange(at + 1)),
            )
            assertEquals("$raw at $at", expected, result.text)
            assertNull(result.composition)
        }
    }

    @Test
    fun `조합 중 한글 갱신은 문자열과 selection composition을 그대로 보존한다`() {
        val previous = TextFieldValue("**한**", TextRange(3), composition = TextRange(2, 3))
        val current = TextFieldValue("**한글**", TextRange(4), composition = TextRange(2, 4))
        assertEquals(current, MarkdownEditActions.processContentChange(previous, current))
    }

    @Test
    fun `밑줄은 식별자 내부에서도 SSOT 토큰 경계로 처리한다`() {
        val raw = "_user_name_"
        assertTrue(MarkdownStyle.ITALIC in MarkdownScanner.activeStyles(TextFieldValue(raw, TextRange(3))))
        assertTrue(MarkdownScanner.activeStyles(TextFieldValue(raw, TextRange(7))).inline.isEmpty())
        val intraword = "prefix_mid_suffix"
        assertTrue(MarkdownStyle.ITALIC in MarkdownScanner.activeStyles(TextFieldValue(intraword, TextRange(9))))
    }

    @Test
    fun `기존 코드 안에 붙여넣을 때 리터럴 공백과 마커를 보존한다`() {
        val raw = "`prefix `"
        val at = raw.length - 1
        val inserted = "** a **"
        val current = TextFieldValue(raw.replaceRange(at, at, inserted), TextRange(at + inserted.length))
        assertEquals(current, MarkdownEditActions.processContentChange(TextFieldValue(raw, TextRange(at)), current))
    }

    @Test
    fun `서로 다른 길이로 닫히는 중첩 서식 시작점에서 Enter는 다음 줄 서식을 보존한다`() {
        val raw = "***a* b**"
        val current = TextFieldValue(raw.replaceRange(3, 3, "\n"), TextRange(4))
        val result = MarkdownEditActions.processContentChange(TextFieldValue(raw, TextRange(3)), current)
        assertEquals("\n$raw", result.text)
        assertEquals(TextRange(1), result.selection)
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
    fun `MarkdownScanner는 이스케이프된 마커를 무시한다`() {
        val escaped = TextFieldValue("""\*기울임아님""", TextRange(7))
        assertFalse(MarkdownStyle.ITALIC in MarkdownScanner.activeStyles(escaped))

    }
}
