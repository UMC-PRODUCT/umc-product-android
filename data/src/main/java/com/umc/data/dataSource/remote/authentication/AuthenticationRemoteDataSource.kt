package com.umc.data.dataSource.remote.authentication

import com.umc.data.response.authentication.MyOAuthResponse
import com.umc.domain.model.base.ApiState

interface AuthenticationRemoteDataSource {
    suspend fun getMyOAuth(): ApiState<List<MyOAuthResponse>>

}