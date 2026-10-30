package com.umc.presentation.community.chatting

import com.umc.domain.model.community.thread.CommunityMessageType
import com.umc.domain.model.community.thread.CommunityThreadMessage
import org.junit.Assert.*
import org.junit.Test

class CommunityDepartureNoticeTest {
    @Test fun serverExitRecordsRemainVisibleAfterReentryWithoutLocalEvents() {
        val left = CommunityThreadMessage(messageId = "101", senderId = "a", type = CommunityMessageType.MEMBER_LEFT,
            content = "A님이 나갔습니다.", createdAt = "2026-10-31T01:00:00Z")
        val kicked = left.copy(messageId = "102", senderId = "b", type = CommunityMessageType.MEMBER_KICKED,
            content = "B님이 내보내졌습니다.", createdAt = "2026-10-31T01:01:00Z")
        val records = listOf(kicked, left)
        val displayed = communityDisplayedMessages(records, emptySet())
        assertEquals(listOf("A님이 나갔습니다.", "B님이 내보내졌습니다."), displayed.map { it.content })
        assertTrue(displayed.all { it.type.isSystemNotice })
        assertTrue(communityUnreadSummaryCandidates(records, emptySet(), 10).isEmpty())
    }
}
