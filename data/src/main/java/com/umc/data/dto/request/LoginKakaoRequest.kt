package com.umc.data.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class LoginKakaoRequest(
    val accessToken: String = "",
) : Request
