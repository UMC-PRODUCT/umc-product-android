package com.umc.domain.usecase.appDataStore.recent

import com.umc.domain.repository.AppDataStoreRepository
import javax.inject.Inject

class GetRecentSearchNoticeUseCase @Inject constructor(
    private val repository: AppDataStoreRepository
) {
    operator fun invoke() = repository.getRecentSearchesNotice()
}
