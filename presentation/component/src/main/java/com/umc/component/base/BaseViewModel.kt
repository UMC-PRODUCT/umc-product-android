package com.umc.component.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.FailState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.text.startsWith

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

    // 단발성 이벤트는 Channel + receiveAsFlow로 전달한다.
    // MutableSharedFlow(replay=0, buffer=0)는 구독자가 0인 순간의 emit을 조용히 버린다.
    // 화면 회전·다크모드 전환으로 컴포지션이 재생성되는 구간에 구독자가 0이 되는데,
    // ViewModel은 살아 있어 그 사이 결과 이벤트를 내보내므로 유실이 실제로 발생했다.
    // Channel은 버퍼에 보관했다가 구독이 붙는 시점에 전달하고, receiveAsFlow가
    // 단일 소비를 보장해 재구독 시 중복 소비도 없다.
    private val _uiEvent = Channel<EVENT>(Channel.BUFFERED)
    val uiEvent: Flow<EVENT> = _uiEvent.receiveAsFlow()

    private val _commonEvent = Channel<CommonViewModelEvent>(Channel.BUFFERED)
    val commonEvent: Flow<CommonViewModelEvent> = _commonEvent.receiveAsFlow()

    // 로딩 상태 관리
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    protected fun updateState(state: STATE.() -> STATE) {
        _uiState.update { it.state() }
    }

    protected fun emitEvent(event: EVENT) {
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }

    private fun emitCommonEvent(event: CommonViewModelEvent) {
        viewModelScope.launch {
            _commonEvent.send(event)
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
                    // 화면별 commonEvent는 구독자가 없을 수 있으므로 전역 신호도 함께 보낸다.
                    // (NavHost 최상단이 이 신호 하나만 관찰해 재로그인으로 보낸다)
                    SessionExpiryBus.notifyExpired()
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