package com.umc.data.response

import kotlinx.serialization.Serializable

@Serializable
data class EmailVerificationCompleteResponse(
    val emailVerificationToken: String
) {
    companion object {
        fun EmailVerificationCompleteResponse.toModel(): String {
            return emailVerificationToken
        }
    }
}
