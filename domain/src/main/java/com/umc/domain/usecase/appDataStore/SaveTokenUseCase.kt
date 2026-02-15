package com.umc.domain.usecase.appDataStore

import com.umc.domain.model.JwtToken
import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.AppDataStoreRepository
import javax.inject.Inject

class SaveTokenUseCase @Inject constructor(
    private val appDataStoreRepository: AppDataStoreRepository
) {
    suspend operator fun invoke(request: JwtToken): ApiState<Unit> {
        return appDataStoreRepository.saveTokens(request.accessToken, request.refreshToken)
    }
}