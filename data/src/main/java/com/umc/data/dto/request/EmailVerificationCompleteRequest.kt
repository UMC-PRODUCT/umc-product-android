package com.umc.data.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class EmailVerificationCompleteRequest(
    val emailVerificationId: Int = 0,
    val verificationCode: String = "",
): Request