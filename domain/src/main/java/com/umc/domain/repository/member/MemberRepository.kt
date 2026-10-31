package com.umc.domain.repository.member

import com.umc.domain.model.JwtToken
import com.umc.domain.model.UserInfo
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.mypage.BusinessCard
import com.umc.domain.model.request.member.EditBusinessCardRequest
import com.umc.domain.model.request.member.RegisterEmailRequest
import com.umc.domain.model.request.member.RegisterRequest
import com.umc.domain.model.request.member.UpdateLinkRequest

interface MemberRepository {
    suspend fun getMyProfile(): ApiState<UserInfo>
    suspend fun getMemberProfile(id: Long): ApiState<UserInfo>
    suspend fun registerOAuth(request: RegisterRequest): ApiState<JwtToken>
    suspend fun registerEmail(request: RegisterEmailRequest): ApiState<JwtToken>

    suspend fun updateMyProfile(profileImageId : String): ApiState<UserInfo>
    suspend fun updateMyLink(request: UpdateLinkRequest): ApiState<UserInfo>
    suspend fun deleteUser(kakaoAccessToken : String, googleAccessToken : String): ApiState<Unit>

    /** 회원 차단. 성공하면 차단 여부(true)를 돌려준다 */
    suspend fun blockMember(memberId: Long): ApiState<Boolean>
    /** 회원 차단 해제. 성공하면 차단 여부(false)를 돌려준다 */
    suspend fun unblockMember(memberId: Long): ApiState<Boolean>
    /** 내가 차단한 회원 ID 목록 */
    suspend fun getBlockedMemberIds(): ApiState<List<Long>>

    /** 내 명함 (다른 회원에게 보이는 그대로, 공개 항목만) */
    suspend fun getMyBusinessCard(): ApiState<BusinessCard>
    /** 명함 수정 화면용 내 연락처·링크 (비공개 항목도 공개 여부와 함께) */
    suspend fun getMyBusinessCardEdit(): ApiState<BusinessCard>
    /** 연락처·링크·공개 여부를 한 번에 저장한다. 성공하면 수정 화면 조회와 같은 값을 돌려준다 */
    suspend fun editMyBusinessCard(request: EditBusinessCardRequest): ApiState<BusinessCard>
}