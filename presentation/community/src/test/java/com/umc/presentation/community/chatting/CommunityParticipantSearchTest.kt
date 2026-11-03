package com.umc.presentation.community.chatting

import com.umc.domain.model.community.thread.CommunityThreadMember
import com.umc.domain.model.community.thread.CommunityThreadRole
import com.umc.domain.model.enums.UserPart
import org.junit.Assert.*
import org.junit.Test

class CommunityParticipantSearchTest {
    @Test fun meThenOwnerThenAdminsThenMembersWithNamesAscendingWithinEachGroup() {
        val participants = listOf(
            CommunityThreadMember(memberId = "member-b", name = "박참여"),
            CommunityThreadMember(memberId = "admin-b", name = "최관리", role = CommunityThreadRole.ADMIN),
            CommunityThreadMember(memberId = "owner", name = "하개설", role = CommunityThreadRole.OWNER),
            CommunityThreadMember(memberId = "member-a", name = "김참여"),
            CommunityThreadMember(memberId = "me", name = "홍본인", role = CommunityThreadRole.ADMIN),
            CommunityThreadMember(memberId = "admin-a", name = "김관리", role = CommunityThreadRole.ADMIN),
        )
        val sorted = sortCommunityParticipants(participants, "me", emptyMap())
        assertEquals(listOf("me", "owner", "admin-a", "admin-b", "member-a", "member-b"), sorted.map { it.memberId })
        assertEquals(listOf("admin-a", "admin-b"), filterCommunityParticipants(sorted, emptyMap(), "관리").map { it.memberId })
        assertEquals("member-b", participants.first().memberId)
    }

    @Test fun ownerWhoIsMeAppearsOnceAndMatchingNamesUseNicknamesAscending() {
        val participants = listOf(
            CommunityThreadMember(memberId = "b", name = "김유엠"),
            CommunityThreadMember(memberId = "owner", name = "홍개설", role = CommunityThreadRole.OWNER),
            CommunityThreadMember(memberId = "a", name = "김유엠"),
        )
        val profiles = mapOf(
            "a" to CommunityParticipantProfile(nickname = "가나다"),
            "b" to CommunityParticipantProfile(nickname = "하하"),
        )
        assertEquals(listOf("owner", "a", "b"), sortCommunityParticipants(participants, "owner", profiles).map { it.memberId })
    }

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
