package com.umc.domain.model.request.notice

import kotlinx.serialization.Serializable

@Serializable
data class NoticeImageRequest(
    val imageIds: List<String>
)
