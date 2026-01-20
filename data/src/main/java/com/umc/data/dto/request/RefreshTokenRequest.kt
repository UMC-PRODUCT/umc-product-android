package com.umc.data.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String = "",
) : Request
