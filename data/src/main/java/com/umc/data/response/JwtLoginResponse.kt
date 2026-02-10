package com.umc.data.response

import com.umc.domain.model.JwtToken
import kotlinx.serialization.Serializable

@Serializable
data class JwtLoginResponse(
    val accessToken: String? = "",
    val refreshToken: String? = "",
    val oAuthVerificationToken: String? = "",
) {
    companion object {
        fun JwtLoginResponse.toModel(): JwtToken {
            return JwtToken(
                accessToken = accessToken ?: "",
                refreshToken = refreshToken ?: "",
                oAuthVerificationToken = oAuthVerificationToken ?: ""
            )
        }
    }
}