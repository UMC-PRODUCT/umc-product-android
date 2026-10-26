package com.umc.data.dataSource.remote

import com.umc.data.api.NotificationApi
import com.umc.data.dataSource.NotificationRemoteDataSource
import com.umc.data.dataSource.base.apiCall
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.ApiResponse
import com.umc.domain.model.request.notification.FcmTokenRequest
import javax.inject.Inject

class NotificationRemoteDataSourceImpl @Inject constructor(
    private val notificationApi: NotificationApi
) : NotificationRemoteDataSource {

    override suspend fun registerFcmToken(request: FcmTokenRequest): ApiState<Unit> =
        apiCall {
            // 스펙의 200 응답에는 JSON 본문이 없다. HTTP 오류는 apiCall에서 처리한다.
            notificationApi.registerFcmToken(request)
            ApiResponse(success = true, code = "200", message = "", result = Unit)
        }
}
