package com.umc.data.api

import com.umc.data.response.authentication.MyOAuthResponse
import com.umc.domain.model.base.ApiResponse
import retrofit2.http.GET

interface AuthenticationApi {

    //OAUTH 연동
    @GET(Endpoints.Authentication.MEMBER_AUTH_SHOW)
    suspend fun getMyOAuth(): ApiResponse<List<MyOAuthResponse>>
}