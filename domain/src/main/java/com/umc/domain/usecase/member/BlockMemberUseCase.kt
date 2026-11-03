package com.umc.domain.usecase.member

import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.member.MemberRepository
import javax.inject.Inject

class BlockMemberUseCase @Inject constructor(
    private val memberRepository: MemberRepository
) {
    // 회원을 차단한다. 성공하면 바뀐 차단 여부를 돌려준다
    suspend operator fun invoke(memberId: Long): ApiState<Boolean> {
        return memberRepository.blockMember(memberId)
    }
}
