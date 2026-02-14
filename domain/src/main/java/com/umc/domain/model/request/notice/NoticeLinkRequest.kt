package com.umc.domain.model.request.notice

import kotlinx.serialization.Serializable

@Serializable
data class NoticeLinkRequest(
    val links : List<String>
)
