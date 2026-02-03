package com.umc.presentation.ui.act.challenge

import com.umc.domain.model.act.challenger.AdminChallenger
import com.umc.domain.model.enums.UserPart
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AdminChallengerViewModel @Inject constructor() :
    BaseViewModel<AdminChallengerUiState, AdminChallengerEvent>(AdminChallengerUiState()) {

    init { loadInitialData() }

    private fun loadInitialData() {
        val dummyList = listOf(
            AdminChallenger(1, "김디자", "닉네임1", 12, UserPart.DESIGN, outCount = 1, warningCount = 0),
            AdminChallenger(2, "홍길동", "닉네임2", 12, UserPart.PM, outCount = 0, warningCount = 1),
            AdminChallenger(3, "이웹마", "닉네임3", 12, UserPart.WEB, outCount = 2, warningCount = 1),
            AdminChallenger(4, "박안드", "닉네임4", 12, UserPart.ANDROID, outCount = 0, warningCount = 0)
        )
        updateState { copy(allChallengers = dummyList, filteredChallengers = dummyList) }
    }

    fun filterList(query: String) {
        val filtered = uiState.value.allChallengers.filter {
            it.name.contains(query) || it.nickname.contains(query)
        }
        updateState { copy(filteredChallengers = filtered) }
    }
}

data class AdminChallengerUiState(
    val allChallengers: List<AdminChallenger> = emptyList(),
    val filteredChallengers: List<AdminChallenger> = emptyList()
) : UiState

sealed class AdminChallengerEvent : UiEvent {
    data class NavigateToDetail(val id: Int) : AdminChallengerEvent()
}