package com.umc.data.response.community

data class CommunityThreadMemberResponse(
    val memberId: String,
    val name: String,
    val part: String,
    val generation: String,
    val role: String,
    val joinedAt: String,
    val state: String,
)