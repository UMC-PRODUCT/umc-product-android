package com.umc.domain.usecase.member

import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.member.MemberRepository
import javax.inject.Inject

class UnblockMemberUseCase @Inject constructor(
    private val memberRepository: MemberRepository
) {
    // 회원 차단을 해제한다. 성공하면 바뀐 차단 여부를 돌려준다
    suspend operator fun invoke(memberId: Long): ApiState<Boolean> {
        return memberRepository.unblockMember(memberId)
    }
}
