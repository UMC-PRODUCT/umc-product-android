package com.umc.data.response.community

data class CommunityThreadListResponse(
    val pinned: List<CommunityThreadSummaryResponse> = emptyList(),
    val threads: List<CommunityThreadSummaryResponse> = emptyList(),
    val nextOffset: String? = null,
    val total: String = "0",
)