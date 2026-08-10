package com.umc.data.request.community

data class CreateCommunityThreadRequest(
    val title: String,
    val description: String?,
    val category: String,
    val icon: String,
    val memberIds: List<Long>,
)
