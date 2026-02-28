package com.umc.data.dataSource.remote

import com.umc.data.api.NotificationApi
import com.umc.data.dataSource.NotificationRemoteDataSource
import com.umc.data.dataSource.base.apiCall
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.notification.FcmTokenRequest
import javax.inject.Inject

class NotificationRemoteDataSourceImpl @Inject constructor(
    private val notificationApi: NotificationApi
) : NotificationRemoteDataSource {

    override suspend fun registerFcmToken(request: FcmTokenRequest): ApiState<Unit> =
        apiCall { notificationApi.registerFcmToken(request) }
}
