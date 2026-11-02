package com.umc.presentation.community.chatting

import com.umc.domain.model.community.thread.CommunityThreadMember
import com.umc.domain.model.community.thread.CommunityThreadRole
import java.text.Collator
import java.util.Locale

data class CommunityParticipantProfile(
    val nickname: String = "",
    val roleLabel: String = "",
)

internal fun communityParticipantDisplayName(
    member: CommunityThreadMember,
    profile: CommunityParticipantProfile?,
): String {
    val name = member.name.ifBlank { "알 수 없는 사용자" }
    val nickname = profile?.nickname?.trim().orEmpty()
    return if (nickname.isNotBlank() && nickname != name && !name.endsWith("($nickname)")) {
        "$name($nickname)"
    } else name
}

internal fun sortCommunityParticipants(
    members: List<CommunityThreadMember>,
    myMemberId: String,
    profiles: Map<String, CommunityParticipantProfile>,
): List<CommunityThreadMember> {
    val names = Collator.getInstance(Locale.KOREAN).apply { strength = Collator.SECONDARY }
    return members.sortedWith(
        compareBy<CommunityThreadMember> {
            when {
                it.memberId == myMemberId -> 0
                it.role == CommunityThreadRole.OWNER -> 1
                it.role == CommunityThreadRole.ADMIN -> 2
                else -> 3
            }
        }.thenComparator { first, second ->
            names.compare(
                communityParticipantDisplayName(first, profiles[first.memberId]).trim(),
                communityParticipantDisplayName(second, profiles[second.memberId]).trim(),
            )
        }.thenBy { it.memberId },
    )
}

/** Search only the current participants; membership and management permissions remain unchanged. */
internal fun filterCommunityParticipants(
    members: List<CommunityThreadMember>,
    profiles: Map<String, CommunityParticipantProfile>,
    query: String,
): List<CommunityThreadMember> {
    val keyword = query.trim()
    if (keyword.isBlank()) return members
    return members.filter { member ->
        member.name.contains(keyword, ignoreCase = true) ||
            profiles[member.memberId]?.nickname?.contains(keyword, ignoreCase = true) == true ||
            member.part?.label?.contains(keyword, ignoreCase = true) == true
    }
}
