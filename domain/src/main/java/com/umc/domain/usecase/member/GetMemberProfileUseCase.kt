package com.umc.domain.usecase.member

import com.umc.domain.model.UserInfo
import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.member.MemberRepository
import javax.inject.Inject

class GetMemberProfileUseCase @Inject constructor(
    private val memberRepository: MemberRepository
) {
    // memberId로 검색한 유저 정보를 뷰모델로 전달합니다.
    suspend operator fun invoke(memberId: Long): ApiState<UserInfo> {
        return memberRepository.getMemberProfile(memberId)
    }
}