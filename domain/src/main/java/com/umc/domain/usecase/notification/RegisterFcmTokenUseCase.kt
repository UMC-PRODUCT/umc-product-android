package com.umc.domain.usecase.notification

import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.NotificationRepository
import javax.inject.Inject

class RegisterFcmTokenUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(fcmToken: String): ApiState<Unit> {
        return notificationRepository.registerFcmToken(fcmToken)
    }
}
