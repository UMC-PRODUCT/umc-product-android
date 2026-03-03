package com.umc.domain.model.request

import kotlinx.serialization.Serializable

@Serializable
data class LoginGoogleRequest(
    val accessToken: String = "",
)