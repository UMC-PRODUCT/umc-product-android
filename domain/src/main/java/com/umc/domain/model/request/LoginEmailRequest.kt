package com.umc.domain.model.request

import kotlinx.serialization.Serializable

@Serializable
data class LoginEmailRequest(
    val email: String = "",
    val password: String = "",
    val clientType: String = "ANDROID",
)
