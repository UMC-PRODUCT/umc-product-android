package com.umc.domain.model.request.organization

import kotlinx.serialization.Serializable

@Serializable
data class CreateChapterRequest(
    val gisuId: Int,
    val name: String,
    val schoolIds: List<Int>
)
