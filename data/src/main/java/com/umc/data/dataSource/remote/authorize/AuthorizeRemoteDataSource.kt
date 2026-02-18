package com.umc.data.dataSource.remote.authorize

import com.umc.data.response.authorization.AuthorAccessResponse
import com.umc.domain.model.base.ApiState

interface AuthorizeRemoteDataSource {
    suspend fun checkAuthAccess(resourceType: String, resourceId: Long): ApiState<AuthorAccessResponse>
}