package com.umc.domain.usecase.appDataStore.recent

import com.umc.domain.repository.AppDataStoreRepository
import javax.inject.Inject

class ClearRecentSearchNoticeUseCase @Inject constructor(
    private val repository: AppDataStoreRepository
) {
    suspend operator fun invoke() = repository.clearRecentSearchNotice()
}
