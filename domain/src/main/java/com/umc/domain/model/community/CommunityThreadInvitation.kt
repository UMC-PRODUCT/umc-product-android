package com.umc.domain.model.community

data class CommunityThreadInvitation(
    val invitedMembers: List<CommunityThreadMember>,
    val memberCount: Int,
)