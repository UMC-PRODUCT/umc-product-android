package com.umc.presentation.ui.act

import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ActViewModel @Inject constructor() : BaseViewModel<ActViewModel.ActivityManagementUiState, ActViewModel.ActivityManagementEvent>(
    ActivityManagementUiState()
) {
    data class ActivityManagementUiState(
        val isAdmin: Boolean = false,
        val temp: String = ""
    ) : UiState

    sealed class ActivityManagementEvent : UiEvent

    fun setAdminMode(isAdmin: Boolean) {
        updateState { copy(isAdmin = isAdmin) }
    }
}