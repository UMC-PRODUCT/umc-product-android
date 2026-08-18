package com.umc.domain.model.request.organization

import kotlinx.serialization.Serializable

@Serializable
data class UpdateStudyGroupRequest(
    val name: String? = null,
    val part: String? = null,
)