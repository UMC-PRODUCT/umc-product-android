package com.umc.data.response.community

data class CommunityThreadInvitablePageResponse(
    val items: List<CommunityThreadInvitableResponse> = emptyList(),
    val nextOffset: String? = null,
    val total: String = "0",
)