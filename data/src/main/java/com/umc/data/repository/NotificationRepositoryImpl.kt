package com.umc.data.repository

import com.umc.data.dataSource.NotificationRemoteDataSource
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.notification.FcmTokenRequest
import com.umc.domain.repository.NotificationRepository
import javax.inject.Inject
import com.google.firebase.installations.FirebaseInstallations
import com.umc.domain.model.base.FailState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class NotificationRepositoryImpl @Inject constructor(
    private val notificationRemoteDataSource: NotificationRemoteDataSource
) : NotificationRepository {

    override suspend fun registerFcmToken(fcmToken: String): ApiState<Unit> = try {
        val installationId = suspendCancellableCoroutine<String> { continuation ->
            FirebaseInstallations.getInstance().id
                .addOnSuccessListener { continuation.resume(it) }
                .addOnFailureListener { continuation.resumeWithException(it) }
                .addOnCanceledListener { continuation.cancel() }
        }
        notificationRemoteDataSource.registerFcmToken(
            FcmTokenRequest(fcmToken = fcmToken, installationId = installationId)
        )
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        ApiState.Fail(FailState(code = "FCM_REGISTRATION_FAILED", message = "알림 기기 등록에 실패했습니다."))
    }
}
