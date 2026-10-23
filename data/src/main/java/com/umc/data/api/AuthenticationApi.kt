package com.umc.data.api

import com.umc.data.response.authentication.MyOAuthResponse
import com.umc.domain.model.base.ApiResponse
import com.umc.data.request.member.AddMyOAuthRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthenticationApi {

    //OAUTH 연동
    @GET(Endpoints.Authentication.MEMBER_AUTH_SHOW)
    suspend fun getMyOAuth(): ApiResponse<List<MyOAuthResponse>>

    //OAUTH 수단 추가(소셜 연동)
    @POST(Endpoints.Authentication.MEMBER_AUTH)
    suspend fun addMyOAuth(
        @Body request: AddMyOAuthRequest
    ): ApiResponse<List<MyOAuthResponse>>
}