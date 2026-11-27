package com.umc.presentation.notice.write

import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NoticeWriteSubmissionTest {
    @Test
    fun `숨긴 빈 서식과 고아 태그는 전송에서 제거하고 빈 본문은 등록하지 않는다`() {
        listOf("<u></u>", "****", "~~~~", "______", "<mark></mark>", "<mark><u></u></mark>",
            "**<u></u>**", "<u>", "</u>", "\u200B", "<u>  </u>").forEach { raw ->
            val state = NoticeWriteUiState(isEditMode = true, title = "제목", content = TextFieldValue(raw))
            assertFalse(raw, state.enableRegister)
            assertTrue(raw, MarkdownEditActions.sanitizeForSubmit(raw).isBlank())
        }
        assertEquals("본문", MarkdownEditActions.sanitizeForSubmit("<u>본문"))
        assertEquals("본문", MarkdownEditActions.sanitizeForSubmit("본문</mark>"))
    }

    @Test
    fun `코드와 이스케이프 및 링크 URL의 태그는 전송 원문을 보존한다`() {
        listOf("`<u></u> ****`", "\\<u\\>", "[링크](https://a.dev/<u>)", "**유효한 서식**").forEach { raw ->
            assertEquals(raw, MarkdownEditActions.sanitizeForSubmit(raw))
            assertTrue(raw, NoticeWriteUiState(isEditMode = true, title = "제목", content = TextFieldValue(raw)).enableRegister)
        }
    }

    @Test
    fun `빈 줄은 저장에서도 유지하고 제출 준비 결과를 등록 조건과 공유한다`() {
        val raw = "\r\n<u></u>\r\n\r\n본문\u200B\r\n"
        val state = NoticeWriteUiState(isEditMode = true, title = "제목", content = TextFieldValue(raw))
        assertEquals("\n\n\n본문\n", state.preparedContent)
        assertEquals(MarkdownEditActions.sanitizeForSubmit(raw), state.preparedContent)
        assertTrue(state.enableRegister)
        assertFalse(state.withContent(TextFieldValue("# ")).enableRegister)
        assertFalse(state.copy(isSubmitting = true).enableRegister)
    }
    @Test
    fun `빈 아티팩트 사이의 같은 서식은 내부 서식까지 병합하고 저장을 반복해도 유지한다`() {
        val cases = mapOf(
            "**가**<u></u>**나**" to "**가나**",
            "*가*<u></u>*나*" to "*가나*",
            "**가**<u></u>*나*" to "**가**_나_",
            "*가*<u></u>**나**" to "_가_**나**",
            "***가***<u></u>**나**" to "**_가_나**",
            "**가**<u></u>***나***" to "**가_나_**",
            "_가_<u></u>_나_" to "_가나_",
            "~~가~~<u></u>~~나~~" to "~~가나~~",
            "**<u>가</u>**<mark></mark>**<u>나</u>**" to "**<u>가나</u>**",
        )
        cases.forEach { (raw, expected) ->
            val prepared = MarkdownEditActions.sanitizeForSubmit(raw)
            assertEquals(raw, expected, prepared)
            assertEquals("가나", MarkdownParser.plainText(prepared))
            assertEquals(prepared, MarkdownEditActions.sanitizeForSubmit(prepared))
        }
    }

    @Test
    fun `별표 경계의 기울임 표기를 바꿔도 리터럴 밑줄과 코드를 보존한다`() {
        val raw = "**가**<u></u>*나_다 `x_y`*"
        val saved = MarkdownEditActions.sanitizeForSubmit(raw)
        assertEquals("**가**_나\\_다 `x_y`_", saved)
        assertEquals("가나_다 x_y", MarkdownParser.plainText(saved))
        assertEquals(saved, MarkdownEditActions.sanitizeForSubmit(saved))
    }

    @Test
    fun `아티팩트가 없는 유효한 서식은 재작성하지 않고 중첩 리터럴도 보존한다`() {
        listOf("*가* **나**", "**가**_나_", "*<u>나_다</u>*", "**_나_**", "[가](https://a.dev/_path_)").forEach { raw ->
            assertEquals(raw, MarkdownEditActions.sanitizeForSubmit(raw))
        }
        val raw = "**가**<u></u>*<u>나_다</u>*"
        val saved = MarkdownEditActions.sanitizeForSubmit(raw)
        assertEquals("**가**_<u>나_다</u>_", saved)
        assertEquals("가나_다", MarkdownParser.plainText(saved))
    }

    @Test
    fun `태그 제거로 첫머리 블록을 새로 만들지 않고 기존 블록은 유지한다`() {
        val cases = mapOf("# 문장" to "\\# 문장", "> 문장" to "\\> 문장", "- 문장" to "\\- 문장",
            "– 문장" to "\\– 문장", "1. 문장" to "1\\. 문장")
        cases.forEach { (literal, expected) ->
            val prepared = MarkdownEditActions.sanitizeForSubmit("<u></u>" + literal)
            assertEquals(expected, prepared)
            assertEquals(literal, MarkdownParser.plainText(prepared))
            assertEquals(prepared, MarkdownEditActions.sanitizeForSubmit(prepared))
            assertEquals(literal, MarkdownEditActions.sanitizeForSubmit(literal))
        }
        assertEquals("**본문**", MarkdownEditActions.sanitizeForSubmit("**본문<u></u>**"))
    }

    @Test
    fun `커서와 다른 필드 변경은 본문 검증 값을 유지하고 본문 변경은 갱신한다`() {
        val state = NoticeWriteUiState(isEditMode = true, title = "제목", content = TextFieldValue("본문"))
        val cursorOnly = state.withContent(TextFieldValue("본문", androidx.compose.ui.text.TextRange(1)))
        assertEquals(state.hasSubmitContent, cursorOnly.hasSubmitContent)
        assertTrue(cursorOnly.copy(title = "다른 제목", aiDownloadPercent = 42).enableRegister)
        assertFalse(cursorOnly.withContent(TextFieldValue("<u></u>")).enableRegister)
        assertTrue(cursorOnly.withContent(TextFieldValue("**변경**")).enableRegister)
        assertFalse(state.javaClass.declaredFields.any { it.type == Lazy::class.java })
    }

    @Test
    fun `빈 목록과 마커뿐인 본문은 등록과 AI 입력에서 모두 거부한다`() {
        listOf("- ", "– ", "1. ", "123. ", "# ", "> ", "<u></u>", "\u200B", "\n- \n1. \n> ").forEach { raw ->
            val state = NoticeWriteUiState(isEditMode = true, title = "제목", content = TextFieldValue(raw))
            assertFalse(raw, state.enableRegister)
            assertNull(raw, prepareNoticeAiInput(raw))
            assertFalse(state.withContent(TextFieldValue("본문")).withContent(TextFieldValue(raw)).enableRegister)
        }
    }

    @Test
    fun `코드와 이스케이프의 목록 모양 리터럴은 유효한 본문과 AI 입력이다`() {
        listOf("`- `", "`1. `", "\\- ", "1\\. ", "\\> ", "# - ", "- 본문", "1. 본문").forEach { raw ->
            val state = NoticeWriteUiState(isEditMode = true, title = "제목", content = TextFieldValue(raw))
            assertTrue(raw, state.enableRegister)
            assertEquals(raw, prepareNoticeAiInput(raw))
        }
        assertEquals("**본문**\n", prepareNoticeAiInput("<u></u>**본문**\u200B\r\n"))
    }

}
