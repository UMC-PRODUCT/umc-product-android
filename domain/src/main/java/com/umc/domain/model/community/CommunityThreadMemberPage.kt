package com.umc.domain.model.community

data class CommunityThreadMemberPage(
    val items: List<CommunityThreadMember>,
    val nextOffset: String?,
    val total: Int,
)