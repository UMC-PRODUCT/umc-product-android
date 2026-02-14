package com.umc.data.request.notice

import kotlinx.serialization.Serializable

@Serializable
data class NoticeLinkRequest(
    val links : List<String>
)
