package com.umc.domain.model.request.notification

import kotlinx.serialization.Serializable

@Serializable
data class FcmTokenRequest(
    val fcmToken: String,
    val installationId: String,
    val platform: String = "ANDROID",
)
