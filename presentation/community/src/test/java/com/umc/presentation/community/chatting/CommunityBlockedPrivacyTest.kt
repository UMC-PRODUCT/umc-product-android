package com.umc.presentation.community.chatting

import com.umc.domain.model.community.thread.CommunityThreadMessage
import org.junit.Assert.*
import org.junit.Test

class CommunityBlockedPrivacyTest {
    private val source = CommunityThreadMessage(messageId = "source", senderId = "2", content = "비공개 원문")
    private val own = source.copy(messageId = "own", senderId = "1")
    private val loaded = CommunityChattingState(threadId = "1", myMemberId = "1", blockedMembersStatus = BlockedMembersStatus.LOADED)

    @Test fun replyPreviewRequiresAKnownUnblockedSource() {
        assertTrue(loaded.canShowReplySnippet(source))
        assertFalse(loaded.copy(blockedMemberIds = setOf("2")).canShowReplySnippet(source))
        assertFalse(loaded.canShowReplySnippet(null))
        assertFalse(loaded.canShowReplySnippet(source.copy(senderId = null)))
    }

    @Test fun unknownOrFailedBlockingDoesNotExposeReplySnippetsOrSummaryInput() {
        for (status in listOf(BlockedMembersStatus.LOADING, BlockedMembersStatus.FAILED)) {
            val state = loaded.copy(blockedMembersStatus = status)
            assertFalse(state.canShowReplySnippet(source))
            assertTrue(state.canShowReplySnippet(own))
            assertTrue(state.summaryInputMessages(listOf(own, source)).isEmpty())
        }
    }

    @Test fun summaryRetryFiltersTheCurrentBlockListEvenAfterReveal() {
        val blocked = loaded.copy(blockedMemberIds = setOf("2"), revealedBlockedMessageIds = setOf("source"))
        assertEquals(listOf(own), blocked.summaryInputMessages(listOf(own, source)))
        assertEquals(listOf(own, source), loaded.summaryInputMessages(listOf(own, source)))
    }

    @Test fun invalidatingSummaryRemovesVisibleTextAndAllSourceMetadata() {
        val state = loaded.copy(unreadSummary = "비공개 요약", unreadSummaryError = "이전 오류", isSummarizingUnread = true,
            aiDownloadPercent = 50, summarizedMessageCount = 1, summaryMessageIds = listOf("source"),
            summaryStart = "start", summaryEnd = "end", summaryTruncated = true).withoutUnreadSummary()
        assertNull(state.unreadSummary)
        assertNull(state.unreadSummaryError)
        assertFalse(state.isSummarizingUnread)
        assertNull(state.aiDownloadPercent)
        assertEquals(0, state.summarizedMessageCount)
        assertTrue(state.summaryMessageIds.isEmpty())
        assertEquals("", state.summaryStart)
        assertEquals("", state.summaryEnd)
        assertFalse(state.summaryTruncated)
    }

    @Test fun cannotRevealMessagesBeforeBlockListIsConfirmed() {
        val state = loaded.copy(blockedMembersStatus = BlockedMembersStatus.FAILED, messages = listOf(source), blockedMemberIds = setOf("2"))
        assertEquals(state, state.toggleBlockedMessage("source"))
    }
}
