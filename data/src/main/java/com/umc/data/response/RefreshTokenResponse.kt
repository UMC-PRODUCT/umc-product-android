package com.umc.data.response

import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenResponse(
    val accessToken: String = "",
) {
    companion object {
        fun RefreshTokenResponse.toModel(): String {
            return accessToken
        }
    }
}
