package com.umc.domain.model.request.notice

import kotlinx.serialization.Serializable

@Serializable
data class NoticeUpdateRequest(
    val title: String,
    val content: String,
    /** 미전송 시 서버가 기본값(false)으로 해석해 필독 고정이 풀리므로 그대로 다시 보낸다 */
    val mustRead: Boolean = false
)