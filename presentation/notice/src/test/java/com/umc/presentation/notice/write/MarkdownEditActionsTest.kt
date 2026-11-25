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
