package com.umc.presentation.community.chatting

import com.umc.domain.model.community.thread.CommunityThreadMessage
import com.umc.domain.model.community.thread.CommunityMessageType
import org.junit.Assert.*
import org.junit.Test

class CommunityBlockedMessageTest {
    @Test fun revealingOneMessageDoesNotRevealOtherMessagesOrChangeBlocking() {
        val messages = listOf("a", "b").map { CommunityThreadMessage(messageId = it, senderId = "2", content = it) }
        val initial = CommunityChattingState(threadId = "1", myMemberId = "1", messages = messages, blockedMemberIds = setOf("2"))
        val revealed = initial.toggleBlockedMessage("a")
        assertEquals(setOf("a"), revealed.revealedBlockedMessageIds)
        assertEquals(initial.blockedMemberIds, revealed.blockedMemberIds)
        assertEquals(messages, revealed.messages)
        assertTrue(revealed.toggleBlockedMessage("a").revealedBlockedMessageIds.isEmpty())
    }

    @Test fun ownUnblockedDeletedAndSystemMessagesCannotBeRevealed() {
        val state = CommunityChattingState(threadId = "1", myMemberId = "1", blockedMemberIds = setOf("1", "2"), messages = listOf(
            CommunityThreadMessage(messageId = "own", senderId = "1"),
            CommunityThreadMessage(messageId = "unblocked", senderId = "3"),
            CommunityThreadMessage(messageId = "deleted", senderId = "2", status = "DELETED"),
            CommunityThreadMessage(messageId = "system", senderId = "2", type = CommunityMessageType.MEMBER_LEFT),
        ))
        for (id in listOf("own", "unblocked", "deleted", "system", "missing")) assertEquals(state, state.toggleBlockedMessage(id))
    }
}
