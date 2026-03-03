package com.umc.domain.usecase.appDataStore.notification

import com.umc.domain.model.home.NotificationItem
import com.umc.domain.repository.AppDataStoreRepository
import javax.inject.Inject

/**
 * 새로운 알림을 DataStore에 추가하는 UseCase
 */
class AddNotificationUseCase @Inject constructor(
    private val repository: AppDataStoreRepository
) {
    suspend operator fun invoke(notification: NotificationItem) {
        repository.addNotification(notification)
    }
}
