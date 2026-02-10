package com.umc.presentation.ui.splash

import androidx.lifecycle.viewModelScope
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
                successCallback = {
                    emitEvent(SplashEvent.MoveToMainEvent)
                },
                errorCallback = {
                    emitEvent(SplashEvent.MoveToLoginEvent)
                }
            )
        }
    }
}

sealed interface SplashEvent : UiEvent {
    object MoveToMainEvent : SplashEvent

    object MoveToLoginEvent : SplashEvent
}
