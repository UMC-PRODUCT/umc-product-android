package com.umc.domain.usecase.member

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.mypage.BusinessCard
import com.umc.domain.repository.member.MemberRepository
import javax.inject.Inject

class GetMyBusinessCardEditUseCase @Inject constructor(
    private val memberRepository: MemberRepository
) {
    // 프로필 정보 편집 화면용 연락처·링크를 가져온다. 비공개 항목도 공개 여부와 함께 담긴다
    suspend operator fun invoke(): ApiState<BusinessCard> {
        return memberRepository.getMyBusinessCardEdit()
    }
}
