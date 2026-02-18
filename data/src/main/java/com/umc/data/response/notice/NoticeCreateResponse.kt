package com.umc.data.response.notice

import kotlinx.serialization.Serializable

@Serializable
data class NoticeCreateResponse(
    val noticeId: Long
)
