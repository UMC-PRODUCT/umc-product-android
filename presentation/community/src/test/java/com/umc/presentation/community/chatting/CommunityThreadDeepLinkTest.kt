package com.umc.presentation.community.chatting

import org.junit.Assert.assertEquals
import org.junit.Test

class CommunityThreadDeepLinkTest {
    @Test fun serverPathAndQueryLinksShareTheSameCustomSchemeDestination() {
        listOf(
            "umc://thread/42",
            "umc://thread?threadId=42",
            "https://api.university.neordinary.com/community/threads?threadId=42",
        ).forEach { source ->
            assertEquals("umc://thread?threadId=42", buildCommunityThreadDeepLink(source))
        }
    }

    @Test fun queryTargetTakesPrecedenceOverThePathAndOtherParameters() {
        assertEquals(
            "umc://thread?threadId=42",
            buildCommunityThreadDeepLink("https://api.university.neordinary.com/community/threads/99?source=share&threadId=42"),
        )
    }
}
