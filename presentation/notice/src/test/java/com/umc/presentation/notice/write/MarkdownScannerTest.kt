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
    fun `커서 뒤에 닫는 마커가 있으면 그 길이를 돌려준다`() {
        val value = valueAt("**가나**", cursor = 4)
        assertEquals(2, MarkdownScanner.closingMarkerLengthAt(value, MarkdownStyle.BOLD))
    }

    @Test
    fun `스타일이 꺼져 있으면 닫는 마커 길이는 없다`() {
        val value = valueAt("가나", cursor = 2)
        assertEquals(null, MarkdownScanner.closingMarkerLengthAt(value, MarkdownStyle.BOLD))
    }

    @Test
    fun `굵게 토글은 활성 상태에서 닫는 마커 뒤로 커서를 옮긴다`() {
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
}
