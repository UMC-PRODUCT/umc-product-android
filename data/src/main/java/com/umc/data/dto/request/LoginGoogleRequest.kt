package com.umc.data.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class LoginGoogleRequest(
    val idToken: String = "",
) : Request