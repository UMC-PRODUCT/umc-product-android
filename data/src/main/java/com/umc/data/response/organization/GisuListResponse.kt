package com.umc.data.response.organization

import kotlinx.serialization.Serializable

@Serializable
data class GisuListResponse(
    val gisuList: List<GisuItemResponse>
)

@Serializable
data class GisuItemResponse(
    val gisuId: Int,
    val generation: Int,
    val isActive: Boolean = true
)