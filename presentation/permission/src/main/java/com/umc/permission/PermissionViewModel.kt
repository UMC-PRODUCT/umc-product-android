package com.umc.permission

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.UserInfo
import com.umc.domain.usecase.member.GetMyProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PermissionViewModel @Inject constructor(
    private val getMyProfileUseCase: GetMyProfileUseCase,
) : BaseViewModel<PermissionUiState, PermissionEvent>(
    PermissionUiState(),
) {

    fun onClickAlarmCheck() {
        updateState { copy(isAlarmCheck = !isAlarmCheck) }
    }

    fun onClickLocationCheck() {
        updateState { copy(isLocationCheck = !isLocationCheck) }
    }

    fun onClickPhotoCheck() {
        updateState { copy(isPhotoCheck = !isPhotoCheck) }
    }

    fun onClickBack() {
        emitEvent(PermissionEvent.MoveToBack)
    }

    /**
     * 세 항목이 모두 체크된 상태면 전체 해제, 하나라도 해제돼 있으면 전체 선택.
     * 알림(필수)·위치·사진 모두를 한 번에 토글하는 "모두 허용" 행에서 호출됨
     */
    fun onClickAllCheck() {
        if (uiState.value.isAlarmCheck && uiState.value.isLocationCheck && uiState.value.isPhotoCheck) {
            updateState { copy(isAlarmCheck = false, isLocationCheck = false, isPhotoCheck = false) }
        } else {
            updateState { copy(isAlarmCheck = true, isLocationCheck = true, isPhotoCheck = true) }
        }
    }

    /** 시스템 권한 다이얼로그 표시를 PermissionRoute에 위임. 실제 권한 요청 로직은 Route에서 처리 */
    fun onClickSignUp() {
        emitEvent(PermissionEvent.ShowPermissionDialog)
    }

    /**
     * 권한 요청 완료 후 내 프로필을 조회해 챌린저 기록 보유 여부로 홈/실패 화면을 결정.
     * PermissionRoute의 ActivityResultCallback에서 호출됨
     */
    fun signUp() = viewModelScope.launch {
        resultResponse(
            response = getMyProfileUseCase(),
            successCallback = { userInfo ->
                if (hasChallengerRecords(userInfo)) {
                    emitEvent(PermissionEvent.MoveToMainEvent)
                } else {
                    emitEvent(PermissionEvent.MoveToFailEvent)
                }
            },
            errorCallback = {
                emitEvent(PermissionEvent.MoveToFailEvent)
            }
        )
    }

    /** roles 또는 challengerRecords 중 하나라도 challengerId > 0 이면 챌린저 기록이 있는 것으로 판단 */
    private fun hasChallengerRecords(userInfo: UserInfo): Boolean {
        val hasRoleChallengerId = userInfo.roles.any { it.challengerId > 0 }
        val hasRecordChallengerId = userInfo.challengerRecords.any { it.challengerId > 0 }
        return hasRoleChallengerId || hasRecordChallengerId
    }
}

data class PermissionUiState(
    val isAlarmCheck: Boolean = false,
    val isLocationCheck: Boolean = false,
    val isPhotoCheck: Boolean = false,
) : UiState

sealed interface PermissionEvent : UiEvent {
    data object MoveToBack : PermissionEvent
    data object MoveToMainEvent : PermissionEvent
    data object MoveToFailEvent : PermissionEvent
    data object ShowPermissionDialog : PermissionEvent
}
