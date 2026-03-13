package com.umc.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.FailState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// JWT 관련 에러 코드 접두사
private const val JWT_ERROR_PREFIX = "JWT"

// 공통 ViewModel 이벤트
sealed interface CommonViewModelEvent {
    data object MoveToSplash : CommonViewModelEvent
}

abstract class BaseViewModel<STATE : UiState, EVENT : UiEvent>(
    initialPageState: STATE,
) : ViewModel() {
    private val _uiState = MutableStateFlow(initialPageState)
    val uiState: StateFlow<STATE>
        get() = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<EVENT>()
    val uiEvent: SharedFlow<EVENT>
        get() = _uiEvent.asSharedFlow()

    private val _commonEvent = MutableSharedFlow<CommonViewModelEvent>()
    val commonEvent: SharedFlow<CommonViewModelEvent> = _commonEvent.asSharedFlow()

    // 로딩 상태 관리
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    protected fun updateState(state: STATE.() -> STATE) {
        _uiState.update { it.state() }
    }

    protected fun emitEvent(event: EVENT) {
        viewModelScope.launch {
            _uiEvent.emit(event)
        }
    }

    private fun emitCommonEvent(event: CommonViewModelEvent) {
        viewModelScope.launch {
            _commonEvent.emit(event)
        }
    }

    protected fun <D> resultResponse(
        response: ApiState<D>,
        successCallback: (D) -> Unit,
        errorCallback: ((FailState) -> Unit)? = null,
    ) {
        // 로딩 종료
        _isLoading.value = false

        when (response) {
            is ApiState.Fail -> {
                // JWT 관련 에러 체크: 토큰 만료/유효하지 않음 등의 JWT 에러 시 SplashFragment로 이동
                stopLoading()
                if (response.failState.code.startsWith(JWT_ERROR_PREFIX)) {
                    emitCommonEvent(CommonViewModelEvent.MoveToSplash)
                }
                errorCallback?.invoke(response.failState)
            }
            is ApiState.Success -> {
                stopLoading()
                successCallback.invoke(response.data)
            }
        }
    }

    // API 호출 전 로딩 시작
    fun startLoading() {
        _isLoading.value = true
    }

    // 로딩 강제 종료 (에러 등에서 사용)
    fun stopLoading() {
        _isLoading.value = false
    }
}
