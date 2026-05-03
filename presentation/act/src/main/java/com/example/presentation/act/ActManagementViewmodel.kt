package com.example.presentation.act

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.usecase.member.GetMyProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActViewModel @Inject constructor(
    private val getMyProfileUseCase: GetMyProfileUseCase
) : BaseViewModel<ActViewModel.ActivityManagementUiState, ActViewModel.ActivityManagementEvent>(
    ActivityManagementUiState()
) {
    data class ActivityManagementUiState(
        val isAdmin: Boolean = false,
        val hasAdminAccess: Boolean = false, // 관리자 권한 유무에 따른 토글 노출 상태
        val temp: String = ""
    ) : UiState

    sealed class ActivityManagementEvent : UiEvent {
        data class ShowToast(val message: String) : ActivityManagementEvent()
    }

    // 관리자 토글을 보여줄 권한 리스트
    private val adminRoles = setOf(
        "SUPER_ADMIN", "CENTRAL_PRESIDENT", "CENTRAL_VICE_PRESIDENT",
        "CENTRAL_OPERATING_TEAM_MEMBER", "CENTRAL_EDUCATION_TEAM_MEMBER",
        "CHAPTER_PRESIDENT", "SCHOOL_PRESIDENT", "SCHOOL_VICE_PRESIDENT",
        "SCHOOL_PART_LEADER", "SCHOOL_ETC_ADMIN"
    )

    init {
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        viewModelScope.launch {
            resultResponse(
                response = getMyProfileUseCase(),
                successCallback = { userInfo ->
                    val canAccess = userInfo.roles.any { it.roleType in adminRoles }
                    updateState { copy(hasAdminAccess = canAccess) }
                },
                errorCallback = { failState ->
                    emitEvent(ActivityManagementEvent.ShowToast(failState.message))
                }
            )
        }
    }

    fun setAdminMode(isAdmin: Boolean) {
        updateState { copy(isAdmin = isAdmin) }
    }
}