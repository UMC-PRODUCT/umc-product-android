package com.umc.presentation.ui.act.check

import com.umc.domain.model.act.check.AdminCheckStats
import com.umc.domain.model.act.check.AdminPendingUser
import com.umc.domain.model.act.check.AdminSessionCheck
import com.umc.domain.model.enums.AdminSessionStatus
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AdminCheckViewModel @Inject constructor() :
    BaseViewModel<AdminCheckUiState, AdminCheckEvent>(AdminCheckUiState()) {

    init {
        loadMockData()
    }

    private fun loadMockData() {
        val mockPendingUsers = listOf(
            AdminPendingUser(1, "홍길동", "닉네임", "중앙대학교", null, "14:05", false),
            AdminPendingUser(2, "홍길동", "닉네임", "중앙대학교", null, "14:05", false),
            AdminPendingUser(3, "홍길동", "닉네임", "중앙대학교", null, "14:05", true, "개인 사정으로 인한 지각"),
            AdminPendingUser(4, "홍길동", "닉네임", "중앙대학교", null, "14:05", true, "교통 체증")
        )

        val domainSessions = listOf(
            AdminSessionCheck(1, "4주차 정기세션", "2024-03-23", "14:00", "18:00", AdminSessionStatus.IN_PROGRESS, AdminCheckStats(85, 40, 34, 3), mockPendingUsers),
            AdminSessionCheck(2, "4주차 정기세션", "2024-03-23", "14:00", "18:00", AdminSessionStatus.COMPLETED, AdminCheckStats(85, 40, 34, 3), emptyList()),
            AdminSessionCheck(3, "4주차 정기세션", "2024-03-23", "14:00", "18:00", AdminSessionStatus.IN_PROGRESS, AdminCheckStats(85, 40, 34, 3), mockPendingUsers),
        )

        updateState { copy(adminSessions = domainSessions.map { AdminSessionUIModel(it) }) }
    }

    fun toggleSessionExpansion(sessionId: Int) {
        updateState {
            val newList = adminSessions.map { uiModel ->
                if (uiModel.session.id == sessionId) {
                    uiModel.copy(isExpanded = !uiModel.isExpanded)
                } else {
                    uiModel
                }
            }
            copy(adminSessions = newList)
        }
    }
}

data class AdminCheckUiState(
    val adminSessions: List<AdminSessionUIModel> = emptyList()
) : UiState

sealed class AdminCheckEvent : UiEvent