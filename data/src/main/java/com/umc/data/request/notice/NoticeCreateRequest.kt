package com.umc.data.request.notice

import kotlinx.serialization.Serializable

@Serializable
data class NoticeCreateRequest(
    val title: String,
    val content: String,
    val shouldNotify: Boolean,
    val targetInfo: NoticeTargetRequest
)

@Serializable
data class NoticeTargetRequest(
    val targetGisuId: Int,
    val targetChapterId: Int?,
    val targetSchoolId: Int?,
    val targetParts: List<String>
)