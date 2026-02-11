package com.umc.data.api


import com.umc.data.request.member.UpdateMyProfileRequest
import com.umc.data.response.JwtLoginResponse
import com.umc.data.response.member.MemberResponse
import com.umc.domain.model.base.ApiResponse
import com.umc.domain.model.request.member.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.PATCH

interface MemberApi {

    //내 프로필 조회
    @GET(Endpoints.Member.MYPROFILE)
    suspend fun getMyProfile(): ApiResponse<MemberResponse>


    //회원 정보 수정
    @PATCH(Endpoints.Member.MEMBER)
    suspend fun updateMyProfile(
        @Body request: UpdateMyProfileRequest
    ) : ApiResponse<MemberResponse>

    /**Deprecated**/
    @GET(Endpoints.Member.MEMBER_PROFILE)
    suspend fun getMemberProfile(
        @Path("memberId") memberId: Long
    ): ApiResponse<MemberResponse>

    @POST(Endpoints.Member.MEMBER_REGISTER)
    suspend fun register(
        @Body request: RegisterRequest
    ): ApiResponse<JwtLoginResponse>

}