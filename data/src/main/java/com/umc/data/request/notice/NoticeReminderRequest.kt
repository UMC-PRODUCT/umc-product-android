package com.umc.data.request.notice

import kotlinx.serialization.Serializable

@Serializable
data class NoticeReminderRequest(
    val targetIds: List<Long>
)