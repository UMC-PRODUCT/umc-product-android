package com.umc.domain.model.community

data class CommunityInvitableMemberPage(
    val items: List<CommunityInvitableMember>,
    val nextOffset: String?,
    val total: Int,
)