package com.umc.domain.model.request

import com.umc.domain.model.enums.EmailVerifyPurpose
import kotlinx.serialization.Serializable

@Serializable
data class EmailVerificationRequest(
    val email: String = "",
    val purpose: EmailVerifyPurpose = EmailVerifyPurpose.REGISTER,
)
