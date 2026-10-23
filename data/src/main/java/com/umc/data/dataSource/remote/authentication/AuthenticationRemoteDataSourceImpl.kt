package com.umc.data.dataSource.remote.authentication

import com.umc.data.api.AuthenticationApi
import com.umc.data.dataSource.base.apiCall
import com.umc.data.request.member.AddMyOAuthRequest
import com.umc.data.response.authentication.MyOAuthResponse

import com.umc.domain.model.base.ApiState
import javax.inject.Inject

class AuthenticationRemoteDataSourceImpl @Inject constructor(
    private val authenticationApi: AuthenticationApi
) : AuthenticationRemoteDataSource{

    override suspend fun getMyOAuth(): ApiState<List<MyOAuthResponse>> {
        return apiCall { authenticationApi.getMyOAuth() }
    }

    override suspend fun addMyOAuth(request: AddMyOAuthRequest): ApiState<List<MyOAuthResponse>> {
        return apiCall { authenticationApi.addMyOAuth(request) }
    }
}