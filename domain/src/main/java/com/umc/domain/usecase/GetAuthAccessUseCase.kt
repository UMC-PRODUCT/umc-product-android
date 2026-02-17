package com.umc.domain.usecase

import com.umc.domain.model.AuthorAccess
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.enums.ResourceType
import com.umc.domain.repository.AuthRepository
import javax.inject.Inject


/**
 * @param resourceType은 ResourceType enum을 사용
 * @param resourceId는 리소스의 고유 식별자 - ex. 일정 id, 커리큘럼 id 등
 * **/

//리소스 권한 조회 usecase
class GetAuthAccessUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(resourceType: ResourceType, resourceId: Long): ApiState<AuthorAccess> {
        val resourceTypeString = resourceType.name
        return authRepository.checkAuthAccess(resourceTypeString, resourceId)
    }

}