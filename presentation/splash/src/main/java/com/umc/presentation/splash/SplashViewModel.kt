package com.umc.presentation.splash

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.UserInfo
import com.umc.domain.usecase.member.GetMyProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getMyProfileUseCase: GetMyProfileUseCase,
) : BaseViewModel<UiState, SplashEvent>(UiState.Default) {

    init {
        initFun()
    }

    private fun initFun() {
        viewModelScope.launch {
            delay(3.seconds)

            // 기록되어있는 내 정보 가져오기
            resultResponse(
                response = getMyProfileUseCase(),
                successCallback = { userInfo ->
                    if (hasChallengerId(userInfo)) {
                        // 기록된 정보가 있으면 메인으로 이동
                        emitEvent(SplashEvent.MoveToMainEvent)
                    } else {
                        // 챌린저 정보가 없으면 코드 입력 페이지로
                        emitEvent(SplashEvent.MoveToSignUpFailEvent)
                    }
                },
                errorCallback = {
                    // 내 정보 가져오기 실패하면 로그인으로 이동
                    emitEvent(SplashEvent.MoveToLoginEvent)
                }
            )
        }
    }

    private fun hasChallengerId(userInfo: UserInfo): Boolean {
        // roles에서 challengerId 확인
        val hasRoleChallengerId = userInfo.roles.any { it.challengerId > 0 }
        // challengerRecords에서 challengerId 확인
        val hasRecordChallengerId = userInfo.challengerRecords.any { it.challengerId > 0 }
        return hasRoleChallengerId || hasRecordChallengerId
    }
}

sealed interface SplashEvent : UiEvent {
    object MoveToMainEvent : SplashEvent

    object MoveToLoginEvent : SplashEvent

    object MoveToSignUpFailEvent : SplashEvent
}