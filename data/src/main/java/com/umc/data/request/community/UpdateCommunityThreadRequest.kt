package com.umc.data.request.community

data class UpdateCommunityThreadRequest(
    val title: String,
    val description: String,
    val category: String,
    val icon: String,
)