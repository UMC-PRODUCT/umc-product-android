package com.umc.data.api

import com.umc.data.response.terms.TermsResponse
import com.umc.domain.model.base.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface TermsApi {

    //약관 타입으로 정의
    @GET(Endpoints.Terms.TERMS_TYPE)
    suspend fun getTermsByType(
        @Path("termsType") termsType: String
    ): ApiResponse<TermsResponse>

    //약관 ID으로 정의
    @GET(Endpoints.Terms.TERMS_ID)
    suspend fun getTermsById(
        @Path("termsId") termsId: Long
    ): ApiResponse<TermsResponse>

}