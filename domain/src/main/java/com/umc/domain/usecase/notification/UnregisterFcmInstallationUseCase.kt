package com.umc.domain.usecase.notification

import com.umc.domain.repository.NotificationRepository
import javax.inject.Inject

class UnregisterFcmInstallationUseCase @Inject constructor(
    private val repository: NotificationRepository,
) {
    suspend operator fun invoke() = repository.unregisterFcmInstallation()
}
