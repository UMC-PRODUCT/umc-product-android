package com.umc.domain.model.request.organization

import kotlinx.serialization.Serializable

@Serializable
data class CreateGisuRequest(
    val number: Int,
    val startAt: String,
    val endAt: String
)