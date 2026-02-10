package com.umc.data.response.organization

import kotlinx.serialization.Serializable

@Serializable
data class ChapterResponse(
    val chapters: List<Chapter>
)

@Serializable
data class Chapter(
    val id: Int,
    val name: String
)