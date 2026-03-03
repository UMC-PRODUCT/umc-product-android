package com.umc.domain.usecase.appDataStore.notification

import com.umc.domain.repository.AppDataStoreRepository
import javax.inject.Inject

/**
 * DataStore에 저장된 모든 알림을 삭제하는 UseCase
 */
class ClearNotificationsUseCase @Inject constructor(
    private val repository: AppDataStoreRepository
) {
    suspend operator fun invoke() {
        repository.clearNotifications()
    }
}
