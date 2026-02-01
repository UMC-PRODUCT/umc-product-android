package com.umc.domain.model.request

import kotlinx.serialization.Serializable

@Serializable
data class LoginKakaoRequest(
    val accessToken: String = "",
)
