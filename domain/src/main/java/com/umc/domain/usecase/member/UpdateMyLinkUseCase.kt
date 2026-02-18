package com.umc.domain.usecase.member

import com.umc.domain.model.UserInfo
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.member.UpdateLinkRequest
import com.umc.domain.repository.member.MemberRepository
import javax.inject.Inject

class UpdateMyLinkUseCase @Inject constructor(
    private val memberRepository: MemberRepository
) {
    suspend operator fun invoke(request: UpdateLinkRequest): ApiState<UserInfo>
    = memberRepository.updateMyLink(request)
}