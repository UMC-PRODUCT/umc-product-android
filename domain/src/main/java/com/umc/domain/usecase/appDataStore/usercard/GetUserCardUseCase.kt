package com.umc.domain.usecase.appDataStore.usercard

import com.umc.domain.model.mypage.UserCard
import com.umc.domain.repository.AppDataStoreRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserCardUseCase @Inject constructor(
    private val repository: AppDataStoreRepository
) {
    operator fun invoke(): Flow<List<UserCard>> = repository.getUserCards()
}