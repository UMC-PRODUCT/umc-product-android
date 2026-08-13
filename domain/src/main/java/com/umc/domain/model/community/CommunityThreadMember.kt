package com.umc.domain.model.community

data class CommunityThreadMember(
    val memberId: String,
    val name: String,
    val part: String,
    val generation: String,
    val role: String,
    val joinedAt: String,
    val state: String,
)