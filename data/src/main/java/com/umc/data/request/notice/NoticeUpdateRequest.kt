package com.umc.data.request.notice

import kotlinx.serialization.Serializable

@Serializable
data class NoticeUpdateRequest(
    val title: String,
    val content: String
)