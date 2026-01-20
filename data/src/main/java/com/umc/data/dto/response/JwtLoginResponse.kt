package com.umc.data.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class JwtLoginResponse(
    val accessToken: String = "",
    val refreshToken: String = "",
)