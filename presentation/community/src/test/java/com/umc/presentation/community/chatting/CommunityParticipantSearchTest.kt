package com.umc.presentation.community.chatting

import com.umc.domain.model.community.thread.CommunityThreadMember
import com.umc.domain.model.enums.UserPart
import org.junit.Assert.*
import org.junit.Test

class CommunityParticipantSearchTest {
    private val members = listOf(
        CommunityThreadMember(memberId = "1", name = "홍길동", part = UserPart.IOS),
        CommunityThreadMember(memberId = "2", name = "김유엠", part = UserPart.ANDROID),
    )
    private val profiles = mapOf("1" to CommunityParticipantProfile(nickname = "별명"))

    @Test fun searchMatchesNameNicknameAndPartWithoutChangingTheFullList() {
        listOf("홍길", "별명", " ios ").forEach { query ->
            assertEquals(listOf(members.first()), filterCommunityParticipants(members, profiles, query))
        }
        assertEquals(listOf(members.last()), filterCommunityParticipants(members, profiles, "Android"))
        assertTrue(filterCommunityParticipants(members, profiles, "없는 이름").isEmpty())
        assertEquals(members, filterCommunityParticipants(members, profiles, "  "))
    }

    @Test fun displayNameIncludesAvailableNicknameWithoutDuplicatingIt() {
        assertEquals("홍길동(별명)", communityParticipantDisplayName(members.first(), profiles["1"]))
        assertEquals("김유엠", communityParticipantDisplayName(members.last(), null))
        assertEquals("홍길동(별명)", communityParticipantDisplayName(
            members.first().copy(name = "홍길동(별명)"), profiles["1"],
        ))
    }
}
