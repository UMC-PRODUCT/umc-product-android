package com.umc.domain.model.request.challenger

import kotlinx.serialization.Serializable

@Serializable
data class ChallengerRecordMemberRequest(
    val code: String
)
