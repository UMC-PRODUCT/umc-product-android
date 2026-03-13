package com.umc.data.response

import kotlinx.serialization.Serializable

@Serializable
data class EmailVerificationResponse(
    val emailVerificationId: String
) {
    companion object {
        fun EmailVerificationResponse.toModel(): String {
            return emailVerificationId
        }
    }
}