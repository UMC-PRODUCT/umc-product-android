package com.umc.data.dto.request

data class EmailVerificationRequest(
    val email: String = "",
): Request
