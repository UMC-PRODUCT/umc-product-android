package com.umc.data.request.notice

import kotlinx.serialization.Serializable

@Serializable
data class NoticeVoteRequest(
    val title: String,
    val isAnonymous: Boolean,
    val allowMultipleChoice: Boolean,
    val startsAt: String,
    val endsAtExclusive: String,
    val options: List<String>
)