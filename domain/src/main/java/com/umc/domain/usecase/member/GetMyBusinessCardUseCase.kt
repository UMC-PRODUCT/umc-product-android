package com.umc.domain.usecase.member

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.mypage.BusinessCard
import com.umc.domain.repository.member.MemberRepository
import javax.inject.Inject

class GetMyBusinessCardUseCase @Inject constructor(
    private val memberRepository: MemberRepository
) {
    // 내 명함의 연락처·링크를 가져온다. 다른 회원에게 보이는 그대로라 공개 항목만 담긴다
    suspend operator fun invoke(): ApiState<BusinessCard> {
        return memberRepository.getMyBusinessCard()
    }
}
