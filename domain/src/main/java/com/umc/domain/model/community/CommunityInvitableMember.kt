package com.umc.domain.model.community

data class CommunityInvitableMember(
    val memberId: String,
    val challengerId: String,
    val name: String,
    val part: String,
    val generation: String,
)