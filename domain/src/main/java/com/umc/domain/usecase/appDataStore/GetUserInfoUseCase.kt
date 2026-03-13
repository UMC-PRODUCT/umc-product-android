package com.umc.domain.usecase.appDataStore

import com.umc.domain.repository.AppDataStoreRepository
import javax.inject.Inject

/**이 USECASE는 DataStore에 저장된 유저 정보를 불러오는 역할
 *
 * API 호출과는 다릅니다!
 * **/

class GetUserInfoUseCase @Inject constructor(
    private val repository: AppDataStoreRepository
) {
    operator fun invoke() = repository.getUserInfo()
}