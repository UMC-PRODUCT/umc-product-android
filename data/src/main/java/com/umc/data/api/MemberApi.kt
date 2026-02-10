package com.umc.data.api

import com.umc.data.response.JwtLoginResponse
import com.umc.data.response.member.MemberResponse
import com.umc.domain.model.base.ApiResponse
import com.umc.domain.model.request.member.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface MemberApi {

    @GET(Endpoints.Member.MYPROFILE)
    suspend fun getMyProfile(): ApiResponse<MemberResponse>

    @GET(Endpoints.Member.MEMBER_PROFILE)
    suspend fun getMemberProfile(
        @Path("memberId") memberId: Long
    ): ApiResponse<MemberResponse>

    @POST(Endpoints.Member.MEMBER_REGISTER)
    suspend fun register(
        @Body request: RegisterRequest
    ): ApiResponse<JwtLoginResponse>

}