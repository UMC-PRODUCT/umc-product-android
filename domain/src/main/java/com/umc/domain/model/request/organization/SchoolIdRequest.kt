package com.umc.domain.model.request.organization

import kotlinx.serialization.Serializable

@Serializable
data class SchoolIdRequest(
    val schoolIds: List<Int>
)
