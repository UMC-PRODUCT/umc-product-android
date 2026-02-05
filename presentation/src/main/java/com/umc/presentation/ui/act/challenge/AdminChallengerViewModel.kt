package com.umc.presentation.ui.act.challenge

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.act.challenger.AdminChallenger
import com.umc.domain.model.act.challenger.ChallengerManageDialogModel
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.enums.PointType
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.request.challenger.ChallengerPointRequest
import com.umc.domain.usecase.GetAdminChallengerDetailUseCase
import com.umc.domain.usecase.GrantChallengerPointUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminChallengerViewModel @Inject constructor(
    private val getAdminChallengerDetailUseCase: GetAdminChallengerDetailUseCase,
    private val grantChallengerPointUseCase: GrantChallengerPointUseCase
) : BaseViewModel<AdminChallengerUiState, AdminChallengerEvent>(AdminChallengerUiState()) {

    init { loadInitialData() }

    fun grantPoint(challengerId: Int, type: PointType, description: String) {
        viewModelScope.launch {
            val request = ChallengerPointRequest(type, description)
            when (val result = grantChallengerPointUseCase(challengerId.toLong(), request)) {
                is ApiState.Success -> {
                    emitEvent(AdminChallengerEvent.ShowManageDialog(result.data))
                }
                is ApiState.Fail -> {
                    emitEvent(AdminChallengerEvent.ShowErrorToast(result.failState.message))
                }
            }
        }
    }

    private fun loadInitialData() {
        val dummyList = listOf(
            AdminChallenger(101, "김디자", "닉네임1", 12, UserPart.DESIGN, outCount = 1, warningCount = 0),
            AdminChallenger(102, "홍길동", "닉네임2", 12, UserPart.PM, outCount = 0, warningCount = 1),
            AdminChallenger(103, "이웹마", "닉네임3", 12, UserPart.WEB, outCount = 2, warningCount = 1),
            AdminChallenger(104, "박안드", "닉네임4", 12, UserPart.ANDROID, outCount = 0, warningCount = 0)
        )
        updateState { copy(allChallengers = dummyList, filteredChallengers = dummyList) }
    }

    fun filterList(query: String) {
        val filtered = uiState.value.allChallengers.filter {
            it.name.contains(query) || it.nickname.contains(query)
        }
        updateState { copy(filteredChallengers = filtered) }
    }

    // 상세 정보 API 호출 함수
    fun onChallengerClicked(id: Int) {
        viewModelScope.launch {
            when (val result = getAdminChallengerDetailUseCase(id.toLong())) {
                is ApiState.Success -> {
                    emitEvent(AdminChallengerEvent.ShowManageDialog(result.data))
                }
                is ApiState.Fail -> {
                    emitEvent(AdminChallengerEvent.ShowErrorToast(result.failState.message))
                }
            }
        }
    }
}

data class AdminChallengerUiState(
    val allChallengers: List<AdminChallenger> = emptyList(),
    val filteredChallengers: List<AdminChallenger> = emptyList()
) : UiState

sealed interface AdminChallengerEvent : UiEvent {
    data class ShowManageDialog(val model: ChallengerManageDialogModel) : AdminChallengerEvent
    data class ShowErrorToast(val message: String) : AdminChallengerEvent
}