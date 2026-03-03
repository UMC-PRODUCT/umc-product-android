package com.umc.domain.model.request

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val accessToken: String = "",
)
