package com.umc.domain.usecase.appDataStore.notification

import com.umc.domain.repository.AppDataStoreRepository
import javax.inject.Inject

/**
 * DataStore에 저장된 알림 목록을 가져오는 UseCase
 */
class GetNotificationsUseCase @Inject constructor(
    private val repository: AppDataStoreRepository
) {
    operator fun invoke() = repository.getNotifications()
}
