package com.umc.presentation.ui.splash

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.UserInfo
import com.umc.domain.usecase.member.GetMyProfileUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
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
            resultResponse(
                response = getMyProfileUseCase(),
                successCallback = { userInfo ->
                    if (hasChallengerId(userInfo)) {
                        emitEvent(SplashEvent.MoveToMainEvent)
                    } else {
                        emitEvent(SplashEvent.MoveToSignUpFailEvent)
                    }
                },
                errorCallback = {
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
