package com.umc.data.repository.authorize

import com.umc.data.dataSource.remote.authorize.AuthorizeRemoteDataSource
import com.umc.data.response.authorization.AuthorAccessResponse.Companion.toModel
import com.umc.domain.model.AuthorAccess
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.map
import com.umc.domain.repository.authorize.AuthorizeRepository
import javax.inject.Inject

class AuthorizeRepositoryImpl @Inject constructor(
    private val authorizeRemoteDataSource: AuthorizeRemoteDataSource
) : AuthorizeRepository {

    override suspend fun checkAuthAccess(resourceType: String, resourceId: Long
    ): ApiState<AuthorAccess> {
        return authorizeRemoteDataSource.checkAuthAccess(resourceType, resourceId).map {
            it.toModel()
        }
    }
}