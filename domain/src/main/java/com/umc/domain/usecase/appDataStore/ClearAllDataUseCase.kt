package com.umc.domain.usecase.appDataStore

import com.umc.domain.repository.AppDataStoreRepository
import javax.inject.Inject

class ClearAllDataUseCase @Inject constructor(
    private val repository: AppDataStoreRepository
) {
    suspend operator fun invoke() = repository.clearAllData()

}