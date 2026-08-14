package com.umc.presentation.act

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.domain.model.enums.RoleType
import com.umc.domain.usecase.member.GetMyProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActViewModel @Inject constructor(
    private val getMyProfileUseCase: GetMyProfileUseCase //내 프로필 정보 조회
) : BaseViewModel<ActUiState, ActEvent>(
    ActUiState()
) {
    fun onAction(action: ActAction) {
        when (action) {
            ActAction.LoadUserInfo -> getUserInfo()
            is ActAction.SetAdminMode -> setAdminMode(action.isAdmin)
        }
    }

    //초기 유저 정보 조회
    //내 정보와 관리자 권한 여부 조회
    fun getUserInfo() {
        viewModelScope.launch {
            resultResponse(
                response = getMyProfileUseCase(),
                successCallback = { userInfo ->
                    val hasAdminAccess = userInfo.roles.any { role ->
                        runCatching { RoleType.valueOf(role.roleType) }.isSuccess
                    }

                    updateState {
                        copy(
                            userInfo = userInfo,
                            hasAdminAccess = hasAdminAccess,
                            isAdmin = isAdmin && hasAdminAccess
                        )
                    }
                },
                errorCallback = { failState ->
                    emitEvent(ActEvent.ShowToast(failState.message))
                }
            )
        }
    }

    //관리자 모드 전환
    fun setAdminMode(isAdmin: Boolean) {
        updateState {
            copy(isAdmin = isAdmin && hasAdminAccess)
        }
    }
}
