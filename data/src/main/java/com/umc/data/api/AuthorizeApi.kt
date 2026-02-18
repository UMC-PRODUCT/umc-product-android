package com.umc.data.api

import com.umc.data.response.authorization.AuthorAccessResponse
import com.umc.domain.model.base.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface AuthorizeApi {

    @GET(Endpoints.Auth.AUTHORIZATION_CHECK)
    suspend fun checkAuthAccess(
        @Query("resourceType") resourceType: String,
        @Query("resourceId") resourceId: Long
    ): ApiResponse<AuthorAccessResponse>

}