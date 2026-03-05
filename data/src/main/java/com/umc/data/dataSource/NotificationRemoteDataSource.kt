package com.umc.data.dataSource

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.notification.FcmTokenRequest

interface NotificationRemoteDataSource {
    suspend fun registerFcmToken(request: FcmTokenRequest): ApiState<Unit>
}
