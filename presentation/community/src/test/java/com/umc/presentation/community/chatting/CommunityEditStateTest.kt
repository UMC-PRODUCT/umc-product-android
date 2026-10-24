package com.umc.presentation.community.chatting

import org.junit.Assert.*
import org.junit.Test

class CommunityEditStateTest {
    @Test fun rejectedEditKeepsDraftAndLateResponsesCannotCloseRetry() {
        val editing = CommunityChattingState(
            threadId = "thread", editingMessageId = "message",
            editDraft = "수정한 내용", editCommandId = "first",
        )
        val failed = editing.rejectEdit("first", "연결 끊김")
        assertEquals("수정한 내용", failed.editDraft)
        assertEquals("message", failed.editingMessageId)
        assertNull(failed.editCommandId)
        val retry = failed.copy(editDraft = "다시 수정한 내용", editCommandId = "retry", editError = null)
        assertEquals(retry, retry.acknowledgeEdit("first"))
        assertEquals(retry, retry.rejectEdit("first", "늦은 거절"))
        assertEquals(retry, retry.rejectEdit(null, "대상을 알 수 없는 거절"))
        val saved = retry.acknowledgeEdit("retry")
        assertNull(saved.editingMessageId)
        assertEquals("", saved.editDraft)
    }
}
