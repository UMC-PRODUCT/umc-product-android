package com.umc.domain.usecase.appDataStore

import com.umc.domain.model.UserInfo
import com.umc.domain.repository.AppDataStoreRepository
import javax.inject.Inject

/**이 USECASE는 DataStore에 저장된 유저 정보를 새로 저장하는 역할
 *
 * API 호출과는 다릅니다!
 * **/

class UpdateUserInfoUseCase @Inject constructor(
    private val repository: AppDataStoreRepository
) {
    suspend operator fun invoke(userInfo : UserInfo) = repository.saveUserInfo(userInfo)
}