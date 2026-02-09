package com.umc.data.api

import com.umc.data.response.member.MemberResponse
import com.umc.domain.model.base.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface MemberApi {

    @GET(Endpoints.Member.MYPROFILE)
    suspend fun getMyProfile(): ApiResponse<MemberResponse>

    @GET(Endpoints.Member.MEMBER_PROFILE)
    suspend fun getMemberProfile(
        @Path("memberId") memberId: Long
    ): ApiResponse<MemberResponse>

}