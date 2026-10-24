package com.umc.domain.usecase.ai

import com.umc.domain.model.community.thread.CommunityMessageType
import com.umc.domain.model.community.thread.CommunityThreadMessage
import org.junit.Assert.*
import org.junit.Test

class ChatSummaryInputTest {
    @Test fun `input excludes deleted images and blank text and keeps source identities`() {
        val input = SummarizeUnreadChatUseCase.prepareInput(listOf(
            CommunityThreadMessage(messageId = "deleted", content = "secret", status = "DELETED"),
            CommunityThreadMessage(messageId = "image", content = "uri", type = CommunityMessageType.IMAGE),
            CommunityThreadMessage(messageId = "blank", content = " "),
            CommunityThreadMessage(messageId = "valid", senderName = "Kim", content = "hello"),
        ))
        assertEquals("Kim: hello", input.source)
        assertEquals(listOf("valid"), input.messages.map { it.messageId })
    }

    @Test fun `character limit records partially included final message`() {
        val input = SummarizeUnreadChatUseCase.prepareInput(listOf(
            CommunityThreadMessage(messageId = "long", content = "x".repeat(12_001)),
            CommunityThreadMessage(messageId = "outside", content = "outside"),
        ))
        assertEquals(12_000, input.source.length)
        assertEquals(listOf("long"), input.messages.map { it.messageId })
        assertTrue(input.truncated)
    }

    @Test fun `at most fifty text messages enter source`() {
        val input = SummarizeUnreadChatUseCase.prepareInput((1..60).map {
            CommunityThreadMessage(messageId = "$it", content = "text")
        })
        assertEquals(50, input.messages.size)
        assertFalse(input.source.contains("51:"))
    }
}
