package com.umc.data.dataSource.remote.member

import com.umc.data.request.member.ReportMemberRequest
import com.umc.data.response.member.ReportMemberResponse

import com.umc.data.request.member.DeleteUserRequest
import com.umc.data.request.member.EditBusinessCardRequest
import com.umc.data.request.member.UpdateMyLinkRequest
import com.umc.data.request.member.UpdateMyProfileRequest
import com.umc.data.response.JwtLoginResponse
import com.umc.data.response.member.BlockedMemberListResponse
import com.umc.data.response.member.BusinessCardResponse
import com.umc.data.response.member.MemberBlockResponse
import com.umc.data.response.member.MemberResponse
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.member.RegisterEmailRequest
import com.umc.domain.model.request.member.RegisterRequest

interface MemberRemoteDataSource {
    suspend fun getMyProfile(): ApiState<MemberResponse>
    suspend fun getMemberProfile(id: Long): ApiState<MemberResponse>
    suspend fun registerOAuth(request: RegisterRequest): ApiState<JwtLoginResponse>
    suspend fun registerEmail(request: RegisterEmailRequest): ApiState<JwtLoginResponse>

    suspend fun updateMyProfile(request: UpdateMyProfileRequest): ApiState<MemberResponse>
    suspend fun updateMyLink(request: UpdateMyLinkRequest): ApiState<MemberResponse>
    suspend fun deleteUser(request: DeleteUserRequest): ApiState<Unit>
    suspend fun reportMember(memberId: Long, request: ReportMemberRequest): ApiState<ReportMemberResponse>
    suspend fun blockMember(memberId: Long): ApiState<MemberBlockResponse>
    suspend fun unblockMember(memberId: Long): ApiState<MemberBlockResponse>
    suspend fun getBlockedMembers(): ApiState<BlockedMemberListResponse>
    suspend fun getMyBusinessCard(): ApiState<BusinessCardResponse>
    suspend fun getMyBusinessCardEdit(): ApiState<BusinessCardResponse>
    suspend fun editMyBusinessCard(request: EditBusinessCardRequest): ApiState<BusinessCardResponse>
}

