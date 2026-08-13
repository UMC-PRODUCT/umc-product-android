package com.umc.domain.usecase.appDataStore.usercard

import com.umc.domain.repository.AppDataStoreRepository
import javax.inject.Inject

class DeleteAllUserCardUseCase @Inject constructor(
    private val repository: AppDataStoreRepository
) {
    suspend operator fun invoke() {
        repository.clearUserCards()
    }
}