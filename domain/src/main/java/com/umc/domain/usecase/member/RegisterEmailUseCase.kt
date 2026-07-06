package com.umc.domain.usecase.member

import com.umc.domain.model.JwtToken
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.member.RegisterEmailRequest
import com.umc.domain.repository.member.MemberRepository
import javax.inject.Inject

class RegisterEmailUseCase @Inject constructor(
    private val memberRepository: MemberRepository,
) {
    suspend operator fun invoke(request: RegisterEmailRequest): ApiState<JwtToken> {
        return memberRepository.registerEmail(request)
    }
}
