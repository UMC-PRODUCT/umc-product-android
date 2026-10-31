package com.umc.domain.usecase.member

import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.member.MemberRepository
import javax.inject.Inject

class GetBlockedMemberIdsUseCase @Inject constructor(
    private val memberRepository: MemberRepository
) {
    // 내가 차단한 회원 ID 목록을 가져온다
    suspend operator fun invoke(): ApiState<List<Long>> {
        return memberRepository.getBlockedMemberIds()
    }
}
