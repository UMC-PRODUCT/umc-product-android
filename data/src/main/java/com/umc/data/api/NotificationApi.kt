package com.umc.data.api

import com.umc.domain.model.base.ApiResponse
import com.umc.domain.model.request.notification.FcmTokenRequest
import retrofit2.http.Body
import retrofit2.http.PUT

interface NotificationApi {

    @PUT(Endpoints.Notification.FCM_TOKEN)
    suspend fun registerFcmToken(
        @Body request: FcmTokenRequest
    ): ApiResponse<Unit>
}
