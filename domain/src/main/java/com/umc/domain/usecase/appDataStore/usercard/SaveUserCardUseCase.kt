package com.umc.domain.usecase.appDataStore.usercard

import com.umc.domain.model.mypage.UserCard
import com.umc.domain.repository.AppDataStoreRepository
import javax.inject.Inject

class SaveUserCardUseCase @Inject constructor(
    private val repository: AppDataStoreRepository
){
    suspend operator fun invoke(card: UserCard) {
        repository.saveUserCard(card)
    }
}