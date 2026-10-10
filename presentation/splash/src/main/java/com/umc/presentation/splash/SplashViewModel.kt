package com.umc.presentation.splash

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.UserInfo
import com.umc.domain.model.base.isNetworkError
import com.umc.domain.usecase.appDataStore.HasSessionTokenUseCase
import com.umc.domain.usecase.member.GetMyProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val hasSessionTokenUseCase: HasSessionTokenUseCase,
) : BaseViewModel<SplashState, SplashEvent>(SplashState()) {

    init {
        checkLoginState(withDelay = true)
    }

    // 네트워크 오류 안내에서 다시 시도 (스플래시 대기 시간 없이 바로 재확인)
    fun retry() {
        updateState { copy(isNetworkError = false) }
        checkLoginState(withDelay = false)
    }

    private fun checkLoginState(withDelay: Boolean) {
        viewModelScope.launch {
            if (withDelay) delay(3.seconds)

            // 저장된 토큰이 없는 사용자(첫 설치·로그아웃 직후)는 오프라인이어도 로그인으로 보낸다
            val hasSession = hasSessionTokenUseCase()

            // 기록되어있는 내 정보 가져오기
            resultResponse(
                response = getMyProfileUseCase(),
                successCallback = { userInfo ->
                    if (hasChallengerId(userInfo)) {
                        // 기록된 정보가 있으면 메인으로 이동
                        emitEvent(SplashEvent.MoveToMainEvent)
                    } else {
                        // 챌린저 정보가 없으면 코드 입력 페이지로
                        emitEvent(SplashEvent.MoveToInputCodeEvent)
                    }
                },
                errorCallback = {
                    if (it.isNetworkError && hasSession) {
                        // 로그인했던 사용자의 네트워크 단절은 인증 실패가 아니므로 로그인으로 보내지 않고 재시도를 안내
                        updateState { copy(isNetworkError = true) }
                    } else {
                        // 내 정보 가져오기 실패하면 로그인으로 이동
                        emitEvent(SplashEvent.MoveToLoginEvent)
                    }
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

data class SplashState(
    val isNetworkError: Boolean = false,
) : UiState

sealed interface SplashEvent : UiEvent {
    object MoveToMainEvent : SplashEvent

    object MoveToLoginEvent : SplashEvent

    object MoveToInputCodeEvent : SplashEvent
}