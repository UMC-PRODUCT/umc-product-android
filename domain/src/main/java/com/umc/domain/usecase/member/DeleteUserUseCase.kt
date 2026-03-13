package com.umc.domain.usecase.member

import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.member.MemberRepository
import javax.inject.Inject

class DeleteUserUseCase @Inject constructor(
    private val memberRepository: MemberRepository
) {
    suspend operator fun invoke(kakaoAccessToken: String, googleAccessToken: String): ApiState<Unit> {
        return memberRepository.deleteUser(kakaoAccessToken, googleAccessToken)
    }
}
