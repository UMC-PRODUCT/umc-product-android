package com.umc.presentation.community.chatting

import org.junit.Assert.*
import org.junit.Test

class CommunityDraftMentionsTest {
    @Test fun queryUsesTheCursorAndDoesNotTreatAnEmailOrCompletedWordAsAMention() {
        assertEquals(CommunityMentionQuery(3, 5, "홍"), communityMentionQuery("안녕 @홍 뒤 문장", 5))
        assertEquals(CommunityMentionQuery(0, 1, ""), communityMentionQuery("@", 1))
        assertNull(communityMentionQuery("user@example.com", 8))
        assertNull(communityMentionQuery("@홍 ", 3))
        assertNull(communityMentionQuery("@@홍", 3))
    }

    @Test fun editingBeforeAMentionMovesItsSpanAndRetainsItsIdentity() {
        val before = "안녕 @홍길동 반가워"
        val mention = CommunityDraftMention("10", "홍길동", 3, 7)
        val state = CommunityChattingState(threadId = "thread", draft = before, draftMentions = listOf(mention))
            .withMentionDraft("안녕하세요 @홍길동 반가워")
        assertEquals(listOf(mention.copy(start = 6, end = 10)), state.draftMentions)
        assertEquals(mapOf("10" to "홍길동"), state.mentionedMembers)
    }

    @Test fun editingOrDeletingAMentionRemovesItsRecipientId() {
        val mention = CommunityDraftMention("10", "홍길동", 0, 4)
        val state = CommunityChattingState("thread", draft = "@홍길동 안녕", draftMentions = listOf(mention))
        assertTrue(state.withMentionDraft("@홍길 안녕").mentionedMembers.isEmpty())
        assertTrue(state.withMentionDraft("안녕").draftMentions.isEmpty())
        assertTrue(state.withMentionDraft("").mentionedMembers.isEmpty())
    }

    @Test fun duplicateNamesKeepTheSelectedIdsAndDeletingOneDoesNotSelectTheOther() {
        val state = CommunityChattingState(
            "thread", draft = "@홍길동 @홍길동 ",
            draftMentions = listOf(
                CommunityDraftMention("10", "홍길동", 0, 4),
                CommunityDraftMention("20", "홍길동", 5, 9),
            ),
        )
        assertEquals(setOf("10", "20"), state.withMentionDraft(state.draft).mentionedMembers.keys)
        val changed = state.withMentionDraft("@홍길동 @홍길 ")
        assertEquals(setOf("10"), changed.mentionedMembers.keys)
        assertTrue(changed.withMentionDraft("@홍길 @홍길 ").mentionedMembers.isEmpty())
    }

    @Test fun typingAnIdenticalNameWithoutSelectingItDoesNotAddARecipient() {
        val state = CommunityChattingState("thread").withMentionDraft("@홍길동 안녕")
        assertTrue(state.mentionedMembers.isEmpty())
    }

    @Test fun deletingTheFirstOfTwoIdenticalNamesKeepsTheSecondPersonsId() {
        val state = CommunityChattingState(
            "thread", draft = "@홍길동 @홍길동 ",
            draftMentions = listOf(
                CommunityDraftMention("10", "홍길동", 0, 4),
                CommunityDraftMention("20", "홍길동", 5, 9),
            ),
        ).withMentionDraft("@홍길동 ", changeStart = 0, changeEnd = 5)
        assertEquals(mapOf("20" to "홍길동"), state.mentionedMembers)
        assertEquals(0, state.draftMentions.single().start)
    }

    @Test fun joiningTextToAMentionInvalidatesItButAddingPunctuationAfterASpaceDoesNot() {
        val state = CommunityChattingState(
            "thread", draft = "@홍길동 ",
            draftMentions = listOf(CommunityDraftMention("10", "홍길동", 0, 4)),
        )
        assertTrue(state.withMentionDraft("@홍길동님 ").mentionedMembers.isEmpty())
        assertTrue(state.withMentionDraft("앞@홍길동 ").mentionedMembers.isEmpty())
        assertEquals(setOf("10"), state.withMentionDraft("@홍길동 안녕!").mentionedMembers.keys)
    }
}
