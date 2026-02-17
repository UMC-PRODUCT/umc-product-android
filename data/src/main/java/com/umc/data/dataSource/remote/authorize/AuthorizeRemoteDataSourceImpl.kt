package com.umc.data.dataSource.remote.authorize

import com.umc.data.api.AuthorizeApi
import com.umc.data.dataSource.base.apiCall
import com.umc.data.response.authorization.AuthorAccessResponse
import com.umc.domain.model.base.ApiState
import javax.inject.Inject

class AuthorizeRemoteDataSourceImpl @Inject constructor(
    private val authorizeApi: AuthorizeApi,
) : AuthorizeRemoteDataSource{

    override suspend fun checkAuthAccess(resourceType: String, resourceId: Long
    ): ApiState<AuthorAccessResponse> {
        return apiCall { authorizeApi.checkAuthAccess(resourceType, resourceId) }
    }
}