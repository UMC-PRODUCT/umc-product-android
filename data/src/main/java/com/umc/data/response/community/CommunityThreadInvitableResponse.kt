package com.umc.data.response.community

data class CommunityThreadInvitableResponse(
    val memberId: String,
    val challengerId: String,
    val name: String,
    val part: String,
    val generation: String,
)