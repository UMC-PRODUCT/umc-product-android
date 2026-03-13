package com.umc.data.dataSource.remote.authentication

import com.umc.data.api.AuthenticationApi
import com.umc.data.dataSource.base.apiCall
import com.umc.data.response.authentication.MyOAuthResponse

import com.umc.domain.model.base.ApiState
import javax.inject.Inject

class AuthenticationRemoteDataSourceImpl @Inject constructor(
    private val authenticationApi: AuthenticationApi
) : AuthenticationRemoteDataSource{

    override suspend fun getMyOAuth(): ApiState<List<MyOAuthResponse>> {
        return apiCall { authenticationApi.getMyOAuth() }
    }
}