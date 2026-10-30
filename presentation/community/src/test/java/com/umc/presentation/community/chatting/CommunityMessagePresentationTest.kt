package com.umc.presentation.community.chatting

import com.umc.domain.model.community.thread.CommunityReplyTo
import com.umc.domain.model.community.thread.CommunityThreadMessage
import org.junit.Assert.*
import org.junit.Test

class CommunityMessagePresentationTest {
    @Test fun entryUnreadRangeNeverIncludesAnAlreadyReadMessageToReplaceAnExcludedRecord() {
        val records = listOf(
            CommunityThreadMessage(messageId = "system", type = com.umc.domain.model.community.thread.CommunityMessageType.MEMBER_LEFT),
            CommunityThreadMessage(messageId = "deleted", status = "DELETED"),
            CommunityThreadMessage(messageId = "unread", content = "미읽음"),
            CommunityThreadMessage(messageId = "read", content = "이미 읽음"),
        )
        val candidates = communityEntryUnreadMessages(records, communityDeletedMessageIds(records), 3)
        assertEquals(listOf("unread"), candidates.map { it.messageId })
        assertEquals(1, candidates.size)
        assertTrue(communityEntryUnreadMessages(records, emptySet(), 0).isEmpty())
        assertTrue(communityEntryUnreadMessages(records, emptySet(), -1).isEmpty())
    }

    @Test fun unreadRangeHandlesUnloadedHistoryAndKnownDeletedIdsWithoutExpandingTheBoundary() {
        val records = listOf(
            CommunityThreadMessage(messageId = "known"),
            CommunityThreadMessage(messageId = "deleted", deletedAt = "2026-10-31T01:00:00Z"),
            CommunityThreadMessage(messageId = "unread"),
        )
        assertTrue(communityEntryUnreadMessages(records, setOf("known"), 2).isEmpty())
        assertEquals(listOf("unread"), communityEntryUnreadMessages(records, setOf("known"), 100).map { it.messageId })
    }

    @Test fun deletedMessagesDoNotConsumeTheUnreadSummaryLimit() {
        val messages = listOf(
            CommunityThreadMessage(messageId = "known-deleted"),
            CommunityThreadMessage(messageId = "timestamp", deletedAt = "2026-10-29T00:00:00Z"),
            CommunityThreadMessage(messageId = "status", status = "DELETED"),
            CommunityThreadMessage(messageId = "first", content = "첫 메시지"),
            CommunityThreadMessage(messageId = "second", content = "두 번째 메시지"),
            CommunityThreadMessage(messageId = "third", content = "세 번째 메시지"),
        )
        val deletedIds = communityDeletedMessageIds(messages, setOf("known-deleted"))
        assertEquals(
            listOf("first", "second"),
            communityUnreadSummaryCandidates(messages, deletedIds, 2).map { it.messageId },
        )
        assertTrue(communityUnreadSummaryCandidates(messages, deletedIds, 0).isEmpty())
    }

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
