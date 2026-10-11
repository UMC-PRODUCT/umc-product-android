package com.umc.domain.usecase.appDataStore

import com.umc.domain.repository.AppDataStoreRepository
import javax.inject.Inject

/** 로컬에 저장된 로그인 토큰이 있는지 확인한다. 서버 호출이 아니라 DataStore 조회다. */
class HasSessionTokenUseCase @Inject constructor(
    private val repository: AppDataStoreRepository
) {
    suspend operator fun invoke(): Boolean =
        repository.getAccessToken().isNotEmpty() || repository.getRefreshToken().isNotEmpty()
}
