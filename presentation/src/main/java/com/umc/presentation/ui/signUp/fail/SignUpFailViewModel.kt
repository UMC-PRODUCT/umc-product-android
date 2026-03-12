package com.umc.presentation.ui.signUp.fail

import androidx.lifecycle.viewModelScope
import com.umc.domain.usecase.appDataStore.ClearTokensUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpFailViewModel
@Inject
constructor(
    private val clearTokensUseCase: ClearTokensUseCase
) : BaseViewModel<UiState, SignUpFailEvent>(
    UiState.Default,
) {
    fun onClickBack() {
        emitEvent(SignUpFailEvent.MoveToBack)
    }

    fun onClickHomePage() {
        emitEvent(SignUpFailEvent.MoveToHomePage)
    }
    
    fun onClickNext() {
        emitEvent(SignUpFailEvent.MoveToCode)
    }

    fun onClickKakaoInquiry() {
        emitEvent(SignUpFailEvent.MoveToKakaoInquiry)
    }

    fun onClickLogout() = viewModelScope.launch {
        clearTokensUseCase()
        emitEvent(SignUpFailEvent.MoveToLogin)
    }
}

sealed interface SignUpFailEvent : UiEvent {
    object MoveToBack : SignUpFailEvent
    object MoveToHomePage : SignUpFailEvent
    object MoveToCode : SignUpFailEvent
    object MoveToKakaoInquiry : SignUpFailEvent
    object MoveToLogin : SignUpFailEvent
}
