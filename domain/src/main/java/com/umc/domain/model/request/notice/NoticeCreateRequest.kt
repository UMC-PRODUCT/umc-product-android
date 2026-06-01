package com.umc.domain.model.request.notice

import kotlinx.serialization.Serializable

@Serializable
data class NoticeCreateRequest(
    val title: String,
    val content: String,
    val shouldNotify: Boolean,
    val mustRead: Boolean = false,
    val targetInfo: NoticeTargetRequest
)

@Serializable
data class NoticeTargetRequest(
    val targetGisuId: Int?,
    val targetChapterId: Int?,
    val targetSchoolId: Int?,
    val targetParts: List<String>,
    val targetNoticeTab: String = "CHALLENGER"
)