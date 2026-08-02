package com.umc.data.response.community

data class CommunityThreadMemberMutationResponse(
    val threadId: String = "",
    val memberId: String = "",
    val role: String = "",
    val state: String = "",
    val memberCount: String = "0",
)