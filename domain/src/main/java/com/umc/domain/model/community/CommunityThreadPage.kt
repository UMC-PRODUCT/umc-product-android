package com.umc.domain.model.community

data class CommunityThreadPage(
    val pinnedThreads: List<CommunityThread>,
    val threads: List<CommunityThread>,
    val nextOffset: String?,
    val total: Int,
)