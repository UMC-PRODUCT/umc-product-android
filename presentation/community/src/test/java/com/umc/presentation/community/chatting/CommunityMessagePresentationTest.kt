package com.umc.presentation.community.chatting

import com.umc.domain.model.community.thread.CommunityReplyTo
import com.umc.domain.model.community.thread.CommunityThreadMessage
import org.junit.Assert.*
import org.junit.Test

class CommunityMessagePresentationTest {
    @Test fun deletedSourceIsHiddenAndItsExistingReplyKeepsOnlyTheDeletionNotice() {
        val source = CommunityThreadMessage(messageId = "source", content = "원문", status = "DELETED")
        val reply = CommunityThreadMessage(
            messageId = "reply", content = "답장 본문",
            replyTo = CommunityReplyTo("source", "홍길동", "원문"),
        )
        val messages = listOf(reply, source)
        val displayed = communityDisplayedMessages(messages, communityDeletedMessageIds(messages))
        assertEquals(listOf("reply"), displayed.map { it.messageId })
        assertEquals("답장 본문", displayed.single().content)
        assertEquals("홍길동", displayed.single().replyTo?.senderName)
        assertEquals(DELETED_REPLY_SNIPPET, displayed.single().replyTo?.snippet)
        assertEquals("원문", reply.replyTo?.snippet)
    }

    @Test fun deletionTimestampHidesTheMessageWithoutAStandalonePlaceholder() {
        val deleted = CommunityThreadMessage(messageId = "deleted", deletedAt = "2026-10-28T00:00:00Z")
        val normal = CommunityThreadMessage(messageId = "normal", content = "정상")
        val messages = listOf(deleted, normal)
        assertEquals(listOf(normal), communityDisplayedMessages(messages, communityDeletedMessageIds(messages)))
    }

    @Test fun knownDeletionSurvivesAnUnloadedSourceAndAStaleHistoryResponse() {
        val reply = CommunityThreadMessage(
            messageId = "reply", replyTo = CommunityReplyTo("source", "이름", "이전 원문"),
        )
        val staleSource = CommunityThreadMessage(messageId = "source", content = "이전 원문")
        val deletedIds = communityDeletedMessageIds(listOf(reply), setOf("source"))
        assertEquals(DELETED_REPLY_SNIPPET, communityDisplayedMessages(listOf(reply), deletedIds).single().replyTo?.snippet)
        assertEquals(listOf("reply"), communityDisplayedMessages(listOf(reply, staleSource), deletedIds).map { it.messageId })
    }

    @Test fun anUnloadedOriginalIsNotAssumedDeleted() {
        val reply = CommunityThreadMessage(
            messageId = "reply", replyTo = CommunityReplyTo("older", "이름", "이전 페이지의 원문"),
        )
        val messages = listOf(reply)
        assertEquals(reply, communityDisplayedMessages(messages, communityDeletedMessageIds(messages)).single())
    }
}
