package com.umc.domain.model.request.organization

import kotlinx.serialization.Serializable

@Serializable
data class EditStudyGroupRequest(
    val name: String
)