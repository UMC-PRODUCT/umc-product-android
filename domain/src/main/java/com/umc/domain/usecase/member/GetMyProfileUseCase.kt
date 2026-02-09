package com.umc.domain.usecase.member

import com.umc.domain.model.UserInfo
import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.member.MemberRepository
import com.umc.domain.usecase.appDataStore.UpdateUserInfoUseCase
import javax.inject.Inject

/**accessToken을 기반으로 내 유저 정보를 가져오는 UseCase입니다.**/
class GetMyProfileUseCase @Inject constructor(
    private val memberRepository: MemberRepository,
) {
    suspend operator fun invoke(): ApiState<UserInfo> {
        // 서버에서 정보를 가져오기
        return memberRepository.getMyProfile()

    }
}