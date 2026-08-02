package com.umc.data.response.community

data class CommunityThreadMemberPageResponse(
    val items: List<CommunityThreadMemberResponse> = emptyList(),
    val nextOffset: String? = null,
    val total: String = "0",
)