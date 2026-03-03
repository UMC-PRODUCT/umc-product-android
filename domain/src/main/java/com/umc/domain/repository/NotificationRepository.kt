package com.umc.domain.repository

import com.umc.domain.model.base.ApiState

interface NotificationRepository {
    suspend fun registerFcmToken(fcmToken: String): ApiState<Unit>
}
