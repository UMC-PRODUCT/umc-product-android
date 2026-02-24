package com.umc.domain.model.request.survey

import kotlinx.serialization.Serializable

@Serializable
data class VoteResponseRequest(
    val optionIds: List<Long>
)
