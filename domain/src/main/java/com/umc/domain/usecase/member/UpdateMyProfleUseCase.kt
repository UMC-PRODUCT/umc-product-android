package com.umc.domain.usecase.member

import com.umc.domain.model.UserInfo
import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.member.MemberRepository
import javax.inject.Inject

class UpdateMyProfileUseCase @Inject constructor(
    private val memberRepository: MemberRepository
) {
    suspend operator fun invoke(profileImageId: String): ApiState<UserInfo> {
        return memberRepository.updateMyProfile(profileImageId)
    }
}