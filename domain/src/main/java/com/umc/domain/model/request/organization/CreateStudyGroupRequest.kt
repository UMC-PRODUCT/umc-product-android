package com.umc.domain.model.request.organization

import kotlinx.serialization.Serializable

@Serializable
data class CreateStudyGroupRequest(
    val name: String,
    val part: String,
    val leaderId: Int,
    val memberIds: List<Int>
)
