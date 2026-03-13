package com.umc.data.response

import com.umc.domain.model.JwtToken
import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenResponse(
    val accessToken: String = "",
    val refreshToken: String = "",
) {
    companion object {
        fun RefreshTokenResponse.toModel(): JwtToken {
            return JwtToken(
                accessToken = accessToken,
                refreshToken = refreshToken
            )
        }
    }
}
