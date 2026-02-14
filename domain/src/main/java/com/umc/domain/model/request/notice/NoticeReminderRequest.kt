package com.umc.domain.model.request.notice

import kotlinx.serialization.Serializable

@Serializable
data class NoticeReminderRequest(
    val targetIds: List<Long>
)