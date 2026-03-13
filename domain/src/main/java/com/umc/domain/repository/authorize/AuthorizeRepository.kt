package com.umc.domain.repository.authorize

import com.umc.domain.model.AuthorAccess
import com.umc.domain.model.base.ApiState

interface AuthorizeRepository {
    suspend fun checkAuthAccess(resourceType: String, resourceId: Long): ApiState<AuthorAccess>
}