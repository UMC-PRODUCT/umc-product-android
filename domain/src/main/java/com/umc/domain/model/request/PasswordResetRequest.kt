package com.umc.domain.model.request

import kotlinx.serialization.Serializable

@Serializable
data class PasswordResetRequest(
    val emailVerificationToken: String = "",
    val newPassword: String = "",
)
