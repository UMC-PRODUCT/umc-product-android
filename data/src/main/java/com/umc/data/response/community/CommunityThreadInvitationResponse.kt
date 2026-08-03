package com.umc.data.response.community

data class CommunityThreadInvitationResponse(
    val invitedMembers: List<CommunityThreadMemberResponse> = emptyList(),
    val memberCount: String = "0",
)