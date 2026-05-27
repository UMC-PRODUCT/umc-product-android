package com.example.presentation.act.normal.challenger

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.act.challenger.ChallengerManageDialogModel
import com.umc.domain.model.act.challenger.UserChallenger
import com.umc.domain.usecase.challenger.GetAdminChallengerDetailUseCase
import com.umc.domain.usecase.challenger.GetChallengerListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PAGE_SIZE = 30

@HiltViewModel
class NormalChallengerViewModel @Inject constructor(
    private val getNormalChallengerListUseCase: GetChallengerListUseCase,
    private val getNormalChallengerDetailUseCase: GetAdminChallengerDetailUseCase,
) : BaseViewModel<NormalChallengerUiState, NormalChallengerEvent>(
    NormalChallengerUiState()
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
                response = getNormalChallengerListUseCase(
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
                    emitEvent(NormalChallengerEvent.ShowToast(failState.message))
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
                response = getNormalChallengerDetailUseCase(challengerId),
                successCallback = { detail ->
                    updateState { copy(selectedChallenger = detail) }
                },
                errorCallback = { failState ->
                    emitEvent(NormalChallengerEvent.ShowToast(failState.message))
                }
            )
        }
    }

    fun dismissChallengerDetail() {
        updateState { copy(selectedChallenger = null) }
    }
}

data class NormalChallengerUiState(
    val searchKeyword: String = "",
    val challengers: List<UserChallenger> = emptyList(),
    val selectedChallenger: ChallengerManageDialogModel? = null,
    val nextCursor: Long? = null,
    val hasNext: Boolean = false,
) : UiState {
    val sections: List<NormalChallengerSectionUi>
        get() = challengers
            .groupBy { it.part.label }
            .map { (partName, members) ->
                NormalChallengerSectionUi(
                    partName = partName,
                    members = members.map { it.toMemberUi() }
                )
            }
}

data class NormalChallengerSectionUi(
    val partName: String,
    val members: List<NormalChallengerMemberUi>,
)

data class NormalChallengerMemberUi(
    val id: Long,
    val nicknameWithName: String,
    val generation: String,
    val roleBadge: String? = null,
)

sealed interface NormalChallengerEvent : UiEvent {
    data class ShowToast(val message: String) : NormalChallengerEvent
}

private fun UserChallenger.toMemberUi(): NormalChallengerMemberUi {
    return NormalChallengerMemberUi(
        id = id,
        nicknameWithName = "$name($nickname)",
        generation = "${generation}기",
        roleBadge = role.displayName?.takeIf { role.isVisible }
    )
}
