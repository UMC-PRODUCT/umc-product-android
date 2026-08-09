package com.umc.domain.model.request

import com.umc.domain.model.enums.ClientType
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val accessToken: String = "",
    val clientType: ClientType = ClientType.ANDROID,
)
