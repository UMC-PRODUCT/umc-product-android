package com.umc.data.api

import com.umc.domain.model.request.notification.FcmTokenRequest
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.DELETE
import retrofit2.http.Path

interface NotificationApi {

    @DELETE("${Endpoints.Notification.FCM_TOKEN}/{installationId}")
    suspend fun unregisterFcmInstallation(@Path("installationId") installationId: String): Unit

    @POST(Endpoints.Notification.FCM_TOKEN)
    suspend fun registerFcmToken(
        @Body request: FcmTokenRequest
    ): Unit
}
