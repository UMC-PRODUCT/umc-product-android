package com.umc.presentation.community.chatting

import com.umc.component.theme.AppStrings

import com.umc.domain.model.community.thread.CommunityThreadMember

data class CommunityParticipantProfile(
    val nickname: String = "",
    val roleLabel: String = "",
)

internal fun communityParticipantDisplayName(
    member: CommunityThreadMember,
    profile: CommunityParticipantProfile?,
): String {
    val name = member.name.ifBlank { AppStrings.CHAT_PARTICIPANT_UNKNOWN }
    val nickname = profile?.nickname?.trim().orEmpty()
    return if (nickname.isNotBlank() && nickname != name && !name.endsWith(AppStrings.CHAT_NICKNAME_SUFFIX_FORMAT.format(nickname))) {
        AppStrings.CHAT_PARTICIPANT_NAME_NICKNAME_FORMAT.format(name, nickname)
    } else name
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
