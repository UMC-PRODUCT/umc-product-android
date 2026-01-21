package com.umc.domain.model.request

import kotlinx.serialization.Serializable

@Serializable
data class EmailVerificationCompleteRequest(
    val emailVerificationId: Int = 0,
    val verificationCode: String = "",
)