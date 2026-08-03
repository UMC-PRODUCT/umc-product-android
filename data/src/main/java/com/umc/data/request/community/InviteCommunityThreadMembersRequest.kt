package com.umc.data.request.community

data class InviteCommunityThreadMembersRequest(
    val memberIds: List<Long>,
)