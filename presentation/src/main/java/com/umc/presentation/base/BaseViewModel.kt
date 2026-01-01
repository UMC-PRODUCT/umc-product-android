package com.umc.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.domain.model.ApiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class BaseViewModel<STATE: UiState, EVENT : UiEvent>(
    initialPageState : STATE,
) : ViewModel() {
    private val _uiState = MutableStateFlow(initialPageState)
    val uiState: StateFlow<STATE>
        get() = _uiState.asStateFlow()

    private val _uiEvent =  MutableSharedFlow<EVENT>()
    val uiEvent: SharedFlow<EVENT>
        get() = _uiEvent.asSharedFlow()

    protected fun updateState(
        state: STATE.() -> STATE
    ) {
        _uiState.update { it.state() }
    }

    protected fun emitEvent(event: EVENT) {
        viewModelScope.launch {
            _uiEvent.emit(event)
        }
    }

    protected fun<D> resultResponse(response: ApiState<D>, successCallback : (D) -> Unit, errorCallback : ((String) -> Unit)? = null){
        when(response){
            is ApiState.Error -> {
                errorCallback?.invoke(response.errorCode)
            }
            is ApiState.Success -> {
                successCallback.invoke(response.result)
            }
            is ApiState.Loading -> {
                //TODO 로딩 화면 같은거
            }
        }
    }
}