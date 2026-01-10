package com.umc.presentation.ui.act

import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ActViewModel @Inject constructor() : BaseViewModel<ActViewModel.ActivityManagementUiState, ActViewModel.ActivityManagementEvent>(
    ActivityManagementUiState()
) {
    val isAdminMode = MutableStateFlow(false)

    data class ActivityManagementUiState(val temp: String = "") : UiState
    sealed class ActivityManagementEvent : UiEvent
}