package com.umc.domain.model.request.organization

import kotlinx.serialization.Serializable

@Serializable
data class CreateStudyGroupRequest(
    val name: String,
    val gisuId: Long,
    val part: String,
    val mentorIds: List<Long>,
    val memberIds: List<Long>,
)