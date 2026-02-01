package com.umc.domain.model.request

import kotlinx.serialization.Serializable

@Serializable
data class EmailVerificationRequest(
    val email: String = "",
)
