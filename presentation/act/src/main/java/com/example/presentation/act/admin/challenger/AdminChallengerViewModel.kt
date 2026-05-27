package com.example.presentation.act.admin.challenger

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.act.challenger.AdminChallenger
import com.umc.domain.model.act.challenger.ChallengerManageDialogModel
import com.umc.domain.usecase.challenger.GetAdminChallengerDetailUseCase
import com.umc.domain.usecase.challenger.GetAdminChallengerListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PAGE_SIZE = 30

@HiltViewModel
class AdminChallengerViewModel @Inject constructor(
    private val getAdminChallengerListUseCase: GetAdminChallengerListUseCase,
    private val getAdminChallengerDetailUseCase: GetAdminChallengerDetailUseCase,
) : BaseViewModel<AdminChallengerUiState, AdminChallengerEvent>(
    AdminChallengerUiState()
) {
    init {
        getChallengers()
    }

    fun onSearchKeywordChanged(keyword: String) {
        updateState { copy(searchKeyword = keyword) }
        getChallengers(keyword = keyword.trim().takeIf { it.isNotEmpty() })
    }

    fun getChallengers(
        cursor: Long? = null,
        keyword: String? = uiState.value.searchKeyword.trim().takeIf { it.isNotEmpty() },
    ) {
        viewModelScope.launch {
            startLoading()
            resultResponse(
                response = getAdminChallengerListUseCase(
                    cursor = cursor,
                    size = PAGE_SIZE,
                    schoolId = null,
                    gisuId = null,
                    keyword = keyword,
                    part = null
                ),
                successCallback = { challengerList ->
                    val challengers = if (cursor == null) {
                        challengerList.challengers
                    } else {
                        uiState.value.challengers + challengerList.challengers
                    }

                    updateState {
                        copy(
                            challengers = challengers,
                            nextCursor = challengerList.nextCursor,
                            hasNext = challengerList.hasNext
                        )
                    }
                },
                errorCallback = { failState ->
                    emitEvent(AdminChallengerEvent.ShowToast(failState.message))
                }
            )
        }
    }

    fun loadNextPage() {
        val state = uiState.value
        if (!state.hasNext) return
        getChallengers(cursor = state.nextCursor)
    }

    fun getChallengerDetail(challengerId: Long) {
        viewModelScope.launch {
            startLoading()
            resultResponse(
                response = getAdminChallengerDetailUseCase(challengerId),
                successCallback = { detail ->
                    updateState { copy(selectedChallenger = detail) }
                },
                errorCallback = { failState ->
                    emitEvent(AdminChallengerEvent.ShowToast(failState.message))
                }
            )
        }
    }

    fun dismissChallengerDetail() {
        updateState { copy(selectedChallenger = null) }
    }
}

data class AdminChallengerUiState(
    val searchKeyword: String = "",
    val challengers: List<AdminChallenger> = emptyList(),
    val selectedChallenger: ChallengerManageDialogModel? = null,
    val nextCursor: Long? = null,
    val hasNext: Boolean = false,
) : UiState {
    val sections: List<AdminChallengerSectionUi>
        get() = challengers
            .groupBy { it.part.label }
            .map { (partName, members) ->
                AdminChallengerSectionUi(
                    partName = partName,
                    members = members.map { it.toMemberUi() }
                )
            }
}

data class AdminChallengerSectionUi(
    val partName: String,
    val members: List<AdminChallengerMemberUi>,
)

data class AdminChallengerMemberUi(
    val id: Long,
    val nicknameWithName: String,
    val generation: String,
    val roleBadge: String? = null,
)

sealed interface AdminChallengerEvent : UiEvent {
    data class ShowToast(val message: String) : AdminChallengerEvent
}

private fun AdminChallenger.toMemberUi(): AdminChallengerMemberUi {
    val badge = when {
        outCount > 0 -> "아웃 $outCount"
        warningCount > 0 -> "경고 $warningCount"
        else -> null
    }

    return AdminChallengerMemberUi(
        id = id.toLong(),
        nicknameWithName = "$name($nickname)",
        generation = "${generation}기",
        roleBadge = badge
    )
}
