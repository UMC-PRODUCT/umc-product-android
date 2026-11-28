package com.umc.presentation.notice.write

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.umc.domain.model.enums.NoticeWriterRole
import com.umc.domain.model.enums.WriteCategoryType
import com.umc.domain.model.notice.WriteCategory
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NoticeWriteTextLimitTest {
    private val validState = NoticeWriteUiState(
        writerRole = NoticeWriterRole.SUPER_ADMIN,
        selectedCategory = WriteCategory(WriteCategoryType.ALL_GISU, "전체 기수"),
        title = "가".repeat(255),
        content = TextFieldValue("나".repeat(3_000)),
    )

    private val modes = listOf(
        validState,
        validState.copy(isEditMode = true, editNoticeId = 1L),
        validState.copy(createdNoticeId = 1L),
    )

    @Test
    fun `생성 수정 재시도에서 제목 255자 본문 3000자를 허용한다`() {
        modes.forEach {
            assertTrue(it.isTextLengthValid)
            assertTrue(it.enableRegister)
        }
    }

    @Test
    fun `생성 수정 재시도에서 제목 256자를 차단한다`() {
        modes.forEach {
            val state = it.copy(title = "가".repeat(256))
            assertFalse(state.isTextLengthValid)
            assertFalse(state.enableRegister)
        }
    }

    @Test
    fun `생성 수정 재시도에서 본문 3001자를 차단한다`() {
        modes.forEach {
            val state = it.copy(content = TextFieldValue("나".repeat(3_001)))
            assertFalse(state.isTextLengthValid)
            assertFalse(state.enableRegister)
        }
    }

    @Test
    fun `빈 값과 제출 중에는 길이가 유효해도 등록할 수 없다`() {
        assertFalse(validState.copy(title = " ").enableRegister)
        assertFalse(validState.copy(content = TextFieldValue("\n")).enableRegister)
        assertFalse(validState.copy(isSubmitting = true).enableRegister)
    }

    @Test
    fun `제목과 본문의 초과 입력과 대량 붙여넣기를 거절한다`() {
        listOf(255, 3_000).forEach { limit ->
            val previous = "가".repeat(limit - 1)
            assertTrue(isNoticeTextChangeAllowed(previous, previous + "나", limit))
            assertFalse(isNoticeTextChangeAllowed(previous, previous + "나다", limit))
            assertFalse(isNoticeTextChangeAllowed("", "가".repeat(limit * 2), limit))
        }
    }

    @Test
    fun `한도에서 같은 길이 교체와 커서 조합 변경을 허용한다`() {
        listOf(255, 3_000).forEach { limit ->
            val previous = "가".repeat(limit)
            assertTrue(isNoticeTextChangeAllowed(previous, "나".repeat(limit), limit))
            assertTrue(isNoticeTextChangeAllowed(previous, previous, limit))
        }
    }

    @Test
    fun `기존 초과 내용은 줄이거나 커서를 이동할 수 있지만 늘리거나 같은 길이로 바꿀 수 없다`() {
        listOf(255, 3_000).forEach { limit ->
            val previous = "가".repeat(limit + 2)
            assertTrue(isNoticeTextChangeAllowed(previous, previous.dropLast(1), limit))
            assertTrue(isNoticeTextChangeAllowed(previous, previous, limit))
            assertFalse(isNoticeTextChangeAllowed(previous, previous + "나", limit))
            assertFalse(isNoticeTextChangeAllowed(previous, "나".repeat(previous.length), limit))
        }
    }

    @Test
    fun `마크다운 서식과 AI 삽입은 변환 후 원문 길이로 제한한다`() {
        val previous = TextFieldValue("가".repeat(3_000), TextRange(0, 3_000))
        val expanded = listOf(
            MarkdownEditActions.applyHeading(previous, MarkdownHeading.TITLE1),
            MarkdownEditActions.toggleBold(previous),
            MarkdownEditActions.toggleItalic(previous),
            MarkdownEditActions.toggleUnderline(previous),
            MarkdownEditActions.toggleStrikethrough(previous),
            MarkdownEditActions.toggleBullet(previous),
            MarkdownEditActions.toggleQuote(previous),
            MarkdownEditActions.toggleHighlight(previous, MarkdownHighlightColor.entries.first()),
            MarkdownEditActions.insertText(previous.copy(selection = TextRange(3_000)), "요약"),
        )
        expanded.forEach {
            assertFalse(isNoticeTextChangeAllowed(previous.text, it.text, 3_000))
        }
        val replacement = MarkdownEditActions.insertText(previous, "요약")
        assertTrue(isNoticeTextChangeAllowed(previous.text, replacement.text, 3_000))
    }

    @Test
    fun `개행으로 자동 추가되는 닫는 마커도 본문 길이에 포함한다`() {
        val text = "**" + "가".repeat(2_997)
        val previous = TextFieldValue(text, TextRange(text.length))
        val input = TextFieldValue(text + "\n", TextRange(text.length + 1))
        val adjusted = MarkdownEditActions.closeMarkersOnNewline(previous, input)
        assertTrue(input.text.length == 3_000)
        assertFalse(isNoticeTextChangeAllowed(previous.text, adjusted.text, 3_000))
    }

    @Test
    fun `길이를 줄이는 서식 해제는 한도에서도 허용한다`() {
        val text = "**" + "가".repeat(2_996) + "**"
        val previous = TextFieldValue(text, TextRange(0, text.length))
        val adjusted = MarkdownEditActions.toggleBold(previous)
        assertTrue(adjusted.text.length < previous.text.length)
        assertTrue(isNoticeTextChangeAllowed(previous.text, adjusted.text, 3_000))
    }
}
