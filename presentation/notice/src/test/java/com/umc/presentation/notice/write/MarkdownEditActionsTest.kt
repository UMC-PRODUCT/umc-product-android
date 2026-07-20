package com.umc.presentation.notice.write

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertEquals
import org.junit.Test

class MarkdownEditActionsTest {

    private fun value(text: String, start: Int, end: Int = start) =
        TextFieldValue(text = text, selection = TextRange(start, end))

    // ---------------- 제목 ----------------

    @Test
    fun `본문 줄에 제목1 적용`() {
        val result = MarkdownEditActions.applyHeading(value("hello", 3), MarkdownHeading.TITLE1)
        assertEquals("# hello", result.text)
        assertEquals(TextRange(5), result.selection)
    }

    @Test
    fun `제목1 줄을 제목2로 교체`() {
        val result = MarkdownEditActions.applyHeading(value("# hello", 4), MarkdownHeading.TITLE2)
        assertEquals("## hello", result.text)
    }

    @Test
    fun `제목 줄을 본문으로 되돌리면 prefix 제거`() {
        val result = MarkdownEditActions.applyHeading(value("## hello", 5), MarkdownHeading.BODY)
        assertEquals("hello", result.text)
    }

    @Test
    fun `여러 줄 중 커서가 있는 줄에만 적용`() {
        val result = MarkdownEditActions.applyHeading(value("one\ntwo\nthree", 5), MarkdownHeading.TITLE3)
        assertEquals("one\n### two\nthree", result.text)
    }

    // ---------------- 굵게/기울임 ----------------

    @Test
    fun `선택 영역 굵게 적용`() {
        val result = MarkdownEditActions.toggleBold(value("hello world", 0, 5))
        assertEquals("**hello** world", result.text)
        assertEquals(TextRange(0, 9), result.selection)
    }

    @Test
    fun `굵게 재적용 시 해제`() {
        val result = MarkdownEditActions.toggleBold(value("**hello** world", 0, 9))
        assertEquals("hello world", result.text)
    }

    @Test
    fun `굵게에 기울임 추가하면 별표 3개`() {
        val result = MarkdownEditActions.toggleItalic(value("**hello**", 0, 9))
        assertEquals("***hello***", result.text)
    }

    @Test
    fun `별표 3개에서 기울임 해제하면 굵게만 남음`() {
        val result = MarkdownEditActions.toggleItalic(value("***hello***", 0, 11))
        assertEquals("**hello**", result.text)
    }

    @Test
    fun `별표 3개에서 굵게 해제하면 기울임만 남음`() {
        val result = MarkdownEditActions.toggleBold(value("***hello***", 0, 11))
        assertEquals("*hello*", result.text)
    }

    @Test
    fun `양끝 공백은 마커 바깥에 유지`() {
        // iOS 파서는 마커 안쪽 공백을 허용하지 않음
        val result = MarkdownEditActions.toggleBold(value("a hello b", 1, 8))
        assertEquals("a **hello** b", result.text)
    }

    @Test
    fun `선택 없이 굵게 누르면 빈 마커 삽입 후 커서는 가운데`() {
        val result = MarkdownEditActions.toggleBold(value("ab", 1))
        assertEquals("a****b", result.text)
        assertEquals(TextRange(3), result.selection)
    }

    // ---------------- 밑줄/취소선 ----------------

    @Test
    fun `밑줄 적용과 해제`() {
        val applied = MarkdownEditActions.toggleUnderline(value("abc", 0, 3))
        assertEquals("<u>abc</u>", applied.text)

        val removed = MarkdownEditActions.toggleUnderline(
            applied.copy(selection = TextRange(0, applied.text.length))
        )
        assertEquals("abc", removed.text)
    }

    @Test
    fun `선택 안쪽만 잡아도 바깥 마커 해제`() {
        val result = MarkdownEditActions.toggleStrikethrough(value("~~abc~~", 2, 5))
        assertEquals("abc", result.text)
    }

    @Test
    fun `취소선 적용`() {
        val result = MarkdownEditActions.toggleStrikethrough(value("abc def", 4, 7))
        assertEquals("abc ~~def~~", result.text)
    }

    // ---------------- VisualTransformation 스모크 ----------------

    @Test
    fun `마크다운 변환은 텍스트를 바꾸지 않고 스타일만 입힘`() {
        val source = "# 제목\n**굵게** *기울임* <u>밑줄</u> ~~취소~~\n[라벨](https://umc.com)"
        val transformed = MarkdownVisualTransformation(Color.Gray, Color.Blue)
            .filter(AnnotatedString(source))

        assertEquals(source, transformed.text.text)
        assertEquals(3, transformed.offsetMapping.originalToTransformed(3))
        assertEquals(7, transformed.offsetMapping.transformedToOriginal(7))
    }
}
