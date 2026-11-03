package com.umc.domain.usecase.member

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.mypage.BusinessCard
import com.umc.domain.model.request.member.EditBusinessCardRequest
import com.umc.domain.repository.member.MemberRepository
import javax.inject.Inject

class EditMyBusinessCardUseCase @Inject constructor(
    private val memberRepository: MemberRepository
) {
    // 연락처·링크·공개 여부를 한 번에 저장한다. 링크는 전체 교체라 5개 타입을 모두 담아 보낸다
    suspend operator fun invoke(request: EditBusinessCardRequest): ApiState<BusinessCard> {
        return memberRepository.editMyBusinessCard(request)
    }
}
