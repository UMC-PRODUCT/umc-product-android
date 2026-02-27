package com.umc.data.repository

import com.umc.data.dataSource.NotificationRemoteDataSource
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.notification.FcmTokenRequest
import com.umc.domain.repository.NotificationRepository
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val notificationRemoteDataSource: NotificationRemoteDataSource
) : NotificationRepository {

    override suspend fun registerFcmToken(fcmToken: String): ApiState<Unit> =
        notificationRemoteDataSource.registerFcmToken(FcmTokenRequest(fcmToken))
}
