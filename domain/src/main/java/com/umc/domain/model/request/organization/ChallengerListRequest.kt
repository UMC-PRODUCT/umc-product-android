package com.umc.domain.model.request.organization

import kotlinx.serialization.Serializable

@Serializable
data class ChallengerListRequest(
    val challengerIds: List<Int>
)