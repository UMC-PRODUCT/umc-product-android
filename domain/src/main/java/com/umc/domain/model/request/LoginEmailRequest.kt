package com.umc.domain.model.request

import com.umc.domain.model.enums.ClientType
import kotlinx.serialization.Serializable

@Serializable
data class LoginEmailRequest(
    val email: String = "",
    val password: String = "",
    val clientType: ClientType = ClientType.ANDROID,
)
