package com.umc.presentation.community.chatting

import com.umc.domain.model.community.chatting.CommunityChatEvent
import com.umc.domain.model.community.thread.CommunityMessageType
import com.umc.domain.model.community.thread.CommunityThreadMember
import com.umc.domain.model.community.thread.CommunityThreadMessage
import org.junit.Assert.*
import org.junit.Test

class CommunityDepartureNoticeTest {
    private val departure = CommunityChatEvent.ThreadStateChanged(
        eventId = "leave-1", threadId = "thread", occurredAt = "2026-10-29T01:00:00Z",
        type = CommunityChatEvent.ThreadStateChanged.Type.MEMBER_LEFT, memberId = "other",
    )

    @Test fun successfulHttpKickAppearsInTheConversationWithoutASocketEvent() {
        val before = CommunityChattingState(
            threadId = "thread", myMemberId = "me",
            members = mapOf("other" to CommunityThreadMember(memberId = "other", name = "홍길동")),
        )
        val after = before.withSuccessfulKickNotice(before, "other", "request", departure.occurredAt)
            .copy(members = emptyMap())
        val displayed = communityDisplayedMessages(
            communityMessagesWithDepartureNotices(after.messages, after.departureNotices), emptySet(),
        )
        assertEquals("홍길동 님이 내보내졌습니다.", displayed.single().content)
        assertTrue(after.messages.isEmpty())
        val event = departure.copy(type = CommunityChatEvent.ThreadStateChanged.Type.MEMBER_KICKED)
        val confirmed = after.withDepartureNotice(event)
        assertEquals(1, confirmed.departureNotices.size)
        assertEquals("홍길동 님이 내보내졌습니다.", confirmed.departureNotices.single().content)
        assertEquals(confirmed, confirmed.withDepartureNotice(event))
    }

    @Test fun socketKickBeforeHttpSuccessDoesNotDuplicateTheNotice() {
        val before = CommunityChattingState(
            threadId = "thread", myMemberId = "me",
            members = mapOf("other" to CommunityThreadMember(memberId = "other", name = "홍길동")),
        )
        val socketState = before.withDepartureNotice(departure.copy(
            type = CommunityChatEvent.ThreadStateChanged.Type.MEMBER_KICKED,
        )).copy(members = emptyMap())
        assertEquals(socketState, socketState.withSuccessfulKickNotice(
            before, "other", "request", departure.occurredAt,
        ))
    }

    @Test fun leaveNoticeUsesTheMemberNameWithoutCreatingAServerMessage() {
        val state = CommunityChattingState(
            threadId = "thread", myMemberId = "me",
            members = mapOf("other" to CommunityThreadMember(memberId = "other", name = "홍길동")),
        ).withDepartureNotice(departure)
        assertEquals("홍길동 님이 나갔습니다.", state.departureNotices.single().content)
        assertEquals(CommunityMessageType.SYSTEM, state.departureNotices.single().type)
        assertTrue(state.messages.isEmpty())
        assertTrue(state.summaryCandidateMessages.isEmpty())
        assertEquals(state, state.withDepartureNotice(departure))
        assertEquals(state, state.withDepartureNotice(departure.copy(memberId = "me", eventId = "own")))
        assertEquals(state, state.withDepartureNotice(departure.copy(
            type = CommunityChatEvent.ThreadStateChanged.Type.UPDATED, eventId = "updated",
        )))
    }

    @Test fun kickNoticeUsesDifferentWordingAndStillExcludesMyOwnKick() {
        val state = CommunityChattingState(
            threadId = "thread", myMemberId = "me",
            members = mapOf("other" to CommunityThreadMember(memberId = "other", name = "홍길동")),
        )
        val kick = departure.copy(type = CommunityChatEvent.ThreadStateChanged.Type.MEMBER_KICKED, eventId = "kick")
        val updated = state.withDepartureNotice(kick)
        assertEquals("홍길동 님이 내보내졌습니다.", updated.departureNotices.single().content)
        assertEquals(updated, updated.withDepartureNotice(kick))
        assertEquals(state, state.withDepartureNotice(kick.copy(memberId = "me")))
        assertEquals("참여자가 내보내졌습니다.", state.copy(members = emptyMap())
            .withDepartureNotice(kick).departureNotices.single().content)
    }

    @Test fun missingMemberUsesPreviousSenderNameAndOtherwiseAGenericNotice() {
        val state = CommunityChattingState(threadId = "thread", messages = listOf(
            CommunityThreadMessage(messageId = "old", senderId = "other", senderName = "김유엠"),
        ))
        assertEquals("김유엠 님이 나갔습니다.", state.withDepartureNotice(departure).departureNotices.single().content)
        assertEquals("참여자가 나갔습니다.", state.copy(messages = emptyList())
            .withDepartureNotice(departure).departureNotices.single().content)
    }

    @Test fun noticesStayBetweenMessagesAfterHistoryRecoveryAndPreferPersistedNotices() {
        val notice = CommunityChattingState(threadId = "thread").withDepartureNotice(departure)
            .departureNotices.single()
        val before = CommunityThreadMessage(messageId = "before", createdAt = "2026-10-29T09:59:00")
        val after = CommunityThreadMessage(messageId = "after", createdAt = "2026-10-29T10:01:00+09:00")
        assertEquals(listOf("before", notice.messageId, "after"), communityDisplayedMessages(
            communityMessagesWithDepartureNotices(listOf(after, before), listOf(notice)), emptySet(),
        ).map { it.messageId })
        val persisted = notice.copy(messageId = "server-id")
        assertEquals(listOf("after", "server-id", "before"), communityMessagesWithDepartureNotices(
            listOf(after, persisted, before), listOf(notice),
        ).map { it.messageId })
    }
}
