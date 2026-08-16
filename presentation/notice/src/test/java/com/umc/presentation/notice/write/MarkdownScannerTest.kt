package com.umc.presentation.notice.write

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 마크다운 마커 스캐너 검증.
 * 툴바 활성 표시와 개행 시 마커 닫기가 같은 스캔 결과를 쓰므로 함께 확인한다
 */
class MarkdownScannerTest {

    private val mark = """<mark color="1,1,0,0.4">"""

    /** 커서를 [text]의 원하는 위치에 둔 값 */
    private fun valueAt(text: String, cursor: Int = text.length) =
        TextFieldValue(text, TextRange(cursor))

    // ---------------------------------------------------------------
    // 활성 스타일 판정 (툴바 indigo500 표시)
    // ---------------------------------------------------------------

    @Test
    fun `마커 안쪽 커서는 해당 스타일이 활성이다`() {
        val styles = MarkdownScanner.activeStyles(valueAt("${mark}가나다"))
        assertTrue(MarkdownStyle.HIGHLIGHT in styles)
    }

    @Test
    fun `닫힌 마커 뒤 커서는 활성이 아니다`() {
        val styles = MarkdownScanner.activeStyles(valueAt("${mark}가나다</mark>라마"))
        assertFalse(MarkdownStyle.HIGHLIGHT in styles)
    }

    @Test
    fun `굵게와 기울임은 별표 개수로 구분된다`() {
        assertTrue(MarkdownStyle.BOLD in MarkdownScanner.activeStyles(valueAt("**가나")))
        assertFalse(MarkdownStyle.ITALIC in MarkdownScanner.activeStyles(valueAt("**가나")))
        assertTrue(MarkdownStyle.ITALIC in MarkdownScanner.activeStyles(valueAt("*가나")))
    }

    @Test
    fun `별표 세 개는 굵게와 기울임이 함께 활성이다`() {
        val styles = MarkdownScanner.activeStyles(valueAt("***가나"))
        assertTrue(MarkdownStyle.BOLD in styles)
        assertTrue(MarkdownStyle.ITALIC in styles)
    }

    @Test
    fun `빈 마커 쌍 사이 커서는 활성이다`() {
        // 툴바로 삽입한 직후 상태 (`****` 가운데)
        val styles = MarkdownScanner.activeStyles(valueAt("****", cursor = 2))
        assertTrue(MarkdownStyle.BOLD in styles)
    }

    @Test
    fun `앞줄에서 닫힌 마커는 다음 줄에 영향을 주지 않는다`() {
        val styles = MarkdownScanner.activeStyles(valueAt("${mark}가나다</mark>\n라마"))
        assertFalse(MarkdownStyle.HIGHLIGHT in styles)
    }

    @Test
    fun `줄 단위 스타일과 제목을 판정한다`() {
        assertTrue(MarkdownScanner.activeStyles(valueAt("- 항목")).isBullet)
        assertTrue(MarkdownScanner.activeStyles(valueAt("> 인용")).isQuote)
        assertEquals(
            MarkdownHeading.TITLE2,
            MarkdownScanner.activeStyles(valueAt("## 제목")).heading,
        )
        assertEquals(
            MarkdownHeading.BODY,
            MarkdownScanner.activeStyles(valueAt("본문")).heading,
        )
    }

    @Test
    fun `제목 줄에서도 글머리 기호를 인식한다`() {
        assertTrue(MarkdownScanner.activeStyles(valueAt("## - 항목")).isBullet)
    }

    // ---------------------------------------------------------------
    // 개행 시 마커 닫기
    // ---------------------------------------------------------------

    @Test
    fun `엔터를 치면 열린 형광펜이 개행 앞에서 닫힌다`() {
        val previous = valueAt("${mark}뭐시기뭐시기")
        val typed = valueAt("${mark}뭐시기뭐시기\n")

        val result = MarkdownEditActions.closeMarkersOnNewline(previous, typed)

        assertEquals("${mark}뭐시기뭐시기</mark>\n", result.text)
        // 커서는 개행 뒤(= 새 줄 시작)에 있어야 이어서 입력할 수 있다
        assertEquals(result.text.length, result.selection.min)
    }

    @Test
    fun `닫힌 마커만 있으면 개행에 아무것도 추가하지 않는다`() {
        val previous = valueAt("${mark}가나다</mark>")
        val typed = valueAt("${mark}가나다</mark>\n")

        val result = MarkdownEditActions.closeMarkersOnNewline(previous, typed)

        assertEquals(typed.text, result.text)
    }

    @Test
    fun `중첩된 마커는 안쪽부터 닫힌다`() {
        val previous = valueAt("**${mark}가나")
        val typed = valueAt("**${mark}가나\n")

        val result = MarkdownEditActions.closeMarkersOnNewline(previous, typed)

        assertEquals("**${mark}가나</mark>**\n", result.text)
    }

    @Test
    fun `개행이 아닌 일반 입력은 건드리지 않는다`() {
        val previous = valueAt("${mark}가나")
        val typed = valueAt("${mark}가나다")

        val result = MarkdownEditActions.closeMarkersOnNewline(previous, typed)

        assertEquals(typed.text, result.text)
        assertEquals(typed.selection, result.selection)
    }

    @Test
    fun `앞줄이 이미 닫혀 있으면 뒷줄 개행에서 앞줄을 다시 닫지 않는다`() {
        val previous = valueAt("${mark}가나</mark>\n다라")
        val typed = valueAt("${mark}가나</mark>\n다라\n")

        val result = MarkdownEditActions.closeMarkersOnNewline(previous, typed)

        assertEquals(typed.text, result.text)
    }

    // ---------------------------------------------------------------
    // 툴바 재클릭 시 비활성화
    // ---------------------------------------------------------------

    @Test
    fun `커서 위치에서 열려 있는 마커를 돌려준다`() {
        val value = valueAt("**가나**", cursor = 4)
        assertEquals(
            MarkdownMarker("**", "**"),
            MarkdownScanner.activeMarkerAt(value, MarkdownStyle.BOLD),
        )
    }

    @Test
    fun `스타일이 꺼져 있으면 열린 마커가 없다`() {
        val value = valueAt("가나", cursor = 2)
        assertEquals(null, MarkdownScanner.activeMarkerAt(value, MarkdownStyle.BOLD))
    }

    @Test
    fun `내용이 있으면 굵게 토글은 닫는 마커 뒤로 커서를 옮긴다`() {
        // `**가나|**` 에서 툴바 굵게를 다시 누른 상황
        val value = valueAt("**가나**", cursor = 4)

        val result = MarkdownEditActions.toggleBold(value)

        assertEquals("**가나**", result.text)
        assertEquals(6, result.selection.min)
    }

    @Test
    fun `굵게 토글은 비활성 상태에서 빈 마커 쌍을 넣는다`() {
        val value = valueAt("가나", cursor = 2)

        val result = MarkdownEditActions.toggleBold(value)

        assertEquals("가나****", result.text)
        assertEquals(4, result.selection.min)
    }

    // ---------------------------------------------------------------
    // 마커 쌍 가운데 커서 (툴바로 막 삽입한 직후)
    // ---------------------------------------------------------------

    @Test
    fun `빈 굵게 쌍 가운데에서는 기울임이 활성이 아니다`() {
        // `**|**` — 커서 뒤의 마커까지 삼켜 `***`(굵게+기울임)로 읽으면 안 된다
        val styles = MarkdownScanner.activeStyles(valueAt("****", cursor = 2))

        assertTrue(MarkdownStyle.BOLD in styles)
        assertFalse(MarkdownStyle.ITALIC in styles)
    }

    @Test
    fun `빈 굵게 쌍 가운데에서 굵게를 다시 누르면 마커가 지워진다`() {
        val value = valueAt("****", cursor = 2)

        val result = MarkdownEditActions.toggleBold(value)

        assertEquals("", result.text)
        assertEquals(0, result.selection.min)
    }

    @Test
    fun `형광펜 빈 쌍 가운데에서 형광펜을 다시 누르면 마커가 지워진다`() {
        val value = valueAt("$mark</mark>", cursor = mark.length)

        val result = MarkdownEditActions.toggleHighlight(value, MarkdownHighlightColor.entries.first())

        assertEquals("", result.text)
        assertEquals(0, result.selection.min)
    }

    // ---------------------------------------------------------------
    // 툴바 연타 — 항상 누른 스타일 하나만 켜지고 꺼져야 한다
    // ---------------------------------------------------------------

    @Test
    fun `굵게를 연타해도 기울임은 켜지지 않는다`() {
        var value = valueAt("", cursor = 0)

        value = MarkdownEditActions.toggleBold(value)
        MarkdownScanner.activeStyles(value).let {
            assertTrue("1회: 굵게 켜짐", MarkdownStyle.BOLD in it)
            assertFalse("1회: 기울임은 꺼짐", MarkdownStyle.ITALIC in it)
        }

        value = MarkdownEditActions.toggleBold(value)
        MarkdownScanner.activeStyles(value).let {
            assertFalse("2회: 굵게 꺼짐", MarkdownStyle.BOLD in it)
            assertFalse("2회: 기울임도 꺼진 채", MarkdownStyle.ITALIC in it)
        }

        value = MarkdownEditActions.toggleBold(value)
        MarkdownScanner.activeStyles(value).let {
            assertTrue("3회: 굵게 다시 켜짐", MarkdownStyle.BOLD in it)
            assertFalse("3회: 기울임은 여전히 꺼짐", MarkdownStyle.ITALIC in it)
        }
    }

    @Test
    fun `굵게 켠 상태에서 기울임을 눌러도 굵게는 유지된다`() {
        var value = MarkdownEditActions.toggleBold(valueAt("", cursor = 0))

        value = MarkdownEditActions.toggleItalic(value)
        MarkdownScanner.activeStyles(value).let {
            assertTrue(MarkdownStyle.BOLD in it)
            assertTrue(MarkdownStyle.ITALIC in it)
        }

        // 기울임만 끄면 굵게는 그대로 남아야 한다
        value = MarkdownEditActions.toggleItalic(value)
        MarkdownScanner.activeStyles(value).let {
            assertTrue(MarkdownStyle.BOLD in it)
            assertFalse(MarkdownStyle.ITALIC in it)
        }
    }

    @Test
    fun `밑줄과 취소선도 연타 시 서로 간섭하지 않는다`() {
        var value = valueAt("", cursor = 0)

        value = MarkdownEditActions.toggleUnderline(value)
        assertTrue(MarkdownStyle.UNDERLINE in MarkdownScanner.activeStyles(value))

        value = MarkdownEditActions.toggleUnderline(value)
        MarkdownScanner.activeStyles(value).let {
            assertFalse(MarkdownStyle.UNDERLINE in it)
            assertFalse(MarkdownStyle.STRIKETHROUGH in it)
        }
        assertEquals("", value.text)

        value = MarkdownEditActions.toggleStrikethrough(value)
        MarkdownScanner.activeStyles(value).let {
            assertTrue(MarkdownStyle.STRIKETHROUGH in it)
            assertFalse(MarkdownStyle.UNDERLINE in it)
        }
    }
}
