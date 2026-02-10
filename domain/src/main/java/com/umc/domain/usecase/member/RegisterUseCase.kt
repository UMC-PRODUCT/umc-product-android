package com.umc.domain.usecase.member

import com.umc.domain.model.JwtToken
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.member.RegisterRequest
import com.umc.domain.repository.member.MemberRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val memberRepository: MemberRepository,
) {
    suspend operator fun invoke(request: RegisterRequest): ApiState<JwtToken> {
        return memberRepository.register(request)
    }
}