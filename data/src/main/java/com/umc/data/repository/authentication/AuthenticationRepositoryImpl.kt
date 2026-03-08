package com.umc.data.repository.authentication

import com.umc.data.dataSource.remote.authentication.AuthenticationRemoteDataSource
import com.umc.data.response.authentication.MyOAuthResponse.Companion.toDomain
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.map
import com.umc.domain.model.mypage.UserOAuthType
import com.umc.domain.repository.authentication.AuthenticationRepository
import javax.inject.Inject

class AuthenticationRepositoryImpl @Inject constructor(
    private val authenticationRemoteDataSource: AuthenticationRemoteDataSource
) : AuthenticationRepository {

    override suspend fun getMyOAuth(): ApiState<List<UserOAuthType>> {
        val response = authenticationRemoteDataSource.getMyOAuth()
        return response.map { dataList ->
            dataList.map { it.toDomain() }

        }
    }

}