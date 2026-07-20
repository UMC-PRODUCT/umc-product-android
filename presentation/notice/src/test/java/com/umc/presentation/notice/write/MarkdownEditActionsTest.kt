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

    // ---------------- VisualTransformation ----------------

    private fun transform(source: String) =
        MarkdownVisualTransformation(Color.Gray, Color.Blue).filter(AnnotatedString(source))

    @Test
    fun `마커 문자는 화면에서 숨겨짐`() {
        assertEquals("제목", transform("# 제목").text.text)
        assertEquals("굵게", transform("**굵게**").text.text)
        assertEquals("밑줄", transform("<u>밑줄</u>").text.text)
        assertEquals("취소", transform("~~취소~~").text.text)
        assertEquals("중첩", transform("<u>**중첩**</u>").text.text)
        assertEquals("제목\n본문 굵게", transform("# 제목\n본문 **굵게**").text.text)
    }

    @Test
    fun `빈 마커 쌍도 숨겨짐`() {
        assertEquals("", transform("****").text.text)
        assertEquals("", transform("~~~~").text.text)
        assertEquals("", transform("<u></u>").text.text)
        assertEquals("ab", transform("a****b").text.text)
    }

    @Test
    fun `링크는 숨기지 않고 그대로 표시`() {
        val source = "[라벨](https://umc.com)"
        assertEquals(source, transform(source).text.text)
    }

    @Test
    fun `원문에서 표시 좌표로의 매핑`() {
        // "**굵게**" → 화면 "굵게" : 원문 2..4(굵게)가 화면 0..2
        val mapping = transform("**굵게**").offsetMapping
        assertEquals(0, mapping.originalToTransformed(0))
        assertEquals(0, mapping.originalToTransformed(2))
        assertEquals(2, mapping.originalToTransformed(4))
        assertEquals(2, mapping.originalToTransformed(6))
    }

    @Test
    fun `표시 좌표 커서는 마커 경계에서 토큰 안쪽으로 매핑`() {
        val mapping = transform("**굵게**").offsetMapping
        // 화면 맨 앞(0) → 여는 마커 뒤(원문 2): 앞에 타이핑해도 굵게 유지
        assertEquals(2, mapping.transformedToOriginal(0))
        // 화면 맨 뒤(2) → 닫는 마커 앞(원문 4): 이어서 타이핑해도 굵게 유지
        assertEquals(4, mapping.transformedToOriginal(2))
    }

    @Test
    fun `빈 마커 쌍 사이로 커서가 들어감`() {
        // "****" 화면 좌표 0 → 원문 2(마커 가운데): 굵게 누른 직후 타이핑하면 굵게 적용
        val mapping = transform("****").offsetMapping
        assertEquals(2, mapping.transformedToOriginal(0))
    }

    @Test
    fun `제목 줄 맨 앞 커서는 prefix 뒤로 매핑`() {
        val mapping = transform("# 제목").offsetMapping
        assertEquals(2, mapping.transformedToOriginal(0))
    }
}
