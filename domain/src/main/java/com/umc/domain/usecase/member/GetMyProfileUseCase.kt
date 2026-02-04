package com.umc.domain.usecase.member

import com.umc.domain.model.UserInfo
import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.member.MemberRepository
import com.umc.domain.usecase.appDataStore.UpdateUserInfoUseCase
import javax.inject.Inject

/**accessToken을 기반으로 내 유저 정보를 가져오는 UseCase입니다.**/
class GetMyProfileUseCase @Inject constructor(
    private val memberRepository: MemberRepository,
    private val updateUserInfoUseCase: UpdateUserInfoUseCase,
) {
    suspend operator fun invoke(): ApiState<UserInfo> {
        // 서버에서 정보를 가져오기
        val result = memberRepository.getMyProfile()

        // 성공했다면 로컬 AppDataStore에 저장
        if (result is ApiState.Success) {
           updateUserInfoUseCase(result.data)
        }

        // 결과를 뷰모델에 바로 돌려주기 (거기서 쓸 땐 Success 확인하기!)
        return result
    }
}