package com.umc.data.api


import com.umc.data.request.member.DeleteUserRequest
import com.umc.data.request.member.EditBusinessCardRequest
import com.umc.data.request.member.UpdateMyLinkRequest
import com.umc.data.request.member.UpdateMyProfileRequest
import com.umc.data.response.JwtLoginResponse
import com.umc.data.response.member.BlockedMemberListResponse
import com.umc.data.response.member.BusinessCardResponse
import com.umc.data.response.member.MemberBlockResponse
import com.umc.data.response.member.MemberResponse
import com.umc.domain.model.base.ApiResponse
import com.umc.domain.model.request.member.RegisterEmailRequest
import com.umc.domain.model.request.member.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.HTTP
import retrofit2.http.PATCH

interface MemberApi {

    //내 프로필 조회
    @GET(Endpoints.Member.MYPROFILE_V2)
    suspend fun getMyProfile(): ApiResponse<MemberResponse>


    //회원 정보 수정
    @PATCH(Endpoints.Member.MEMBER)
    suspend fun updateMyProfile(
        @Body request: UpdateMyProfileRequest
    ) : ApiResponse<MemberResponse>

    //회원 링크 수정
    @PATCH(Endpoints.Member.MEMBER_PROFILE_LINK)
    suspend fun updateMyLink(
        @Body request: UpdateMyLinkRequest
    ) : ApiResponse<MemberResponse>


    @GET(Endpoints.Member.MEMBER_PROFILE)
    suspend fun getMemberProfile(
        @Path("memberId") memberId: Long
    ): ApiResponse<MemberResponse>

    //소셜 회원가입
    @POST(Endpoints.Member.MEMBER_REGISTER_OAUTH)
    suspend fun registerOAuth(
        @Body request: RegisterRequest
    ): ApiResponse<JwtLoginResponse>

    //이메일 회원가입
    @POST(Endpoints.Member.MEMBER_REGISTER_EMAIL)
    suspend fun registerEmail(
        @Body request: RegisterEmailRequest
    ): ApiResponse<JwtLoginResponse>

    @DELETE(Endpoints.Member.MEMBER)
    suspend fun deleteUser(
        //@Body request: DeleteUserRequest
    ): ApiResponse<Unit>

    //회원 차단
    @POST(Endpoints.Member.MEMBER_BLOCK)
    suspend fun blockMember(
        @Path("memberId") memberId: Long
    ): ApiResponse<MemberBlockResponse>

    //회원 차단 해제
    @DELETE(Endpoints.Member.MEMBER_BLOCK)
    suspend fun unblockMember(
        @Path("memberId") memberId: Long
    ): ApiResponse<MemberBlockResponse>

    //내가 차단한 회원 목록
    @GET(Endpoints.Member.MEMBER_BLOCKS)
    suspend fun getBlockedMembers(): ApiResponse<BlockedMemberListResponse>

    //내 명함 (공개 항목만)
    @GET(Endpoints.Member.MY_BUSINESS_CARD)
    suspend fun getMyBusinessCard(): ApiResponse<BusinessCardResponse>

    //명함 수정 화면 (비공개 항목도 공개 여부와 함께)
    @GET(Endpoints.Member.MY_BUSINESS_CARD_EDIT)
    suspend fun getMyBusinessCardEdit(): ApiResponse<BusinessCardResponse>

    //연락처·링크·공개 여부 수정
    @PATCH(Endpoints.Member.MY_BUSINESS_CARD)
    suspend fun editMyBusinessCard(
        @Body request: EditBusinessCardRequest
    ): ApiResponse<BusinessCardResponse>

}