package com.umc.presentation.act.normal.challenger

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.act.challenger.ChallengerInfoDialogModel
import com.umc.domain.model.act.challenger.UserChallenger
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.enums.UserPart
import com.umc.domain.usecase.challenger.GetChallengerDetailUseCase
import com.umc.domain.usecase.challenger.GetChallengerListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PAGE_SIZE = 30

@HiltViewModel
class NormalChallengerViewModel @Inject constructor(
    private val getNormalChallengerListUseCase: GetChallengerListUseCase,
    private val getNormalChallengerDetailUseCase: GetChallengerDetailUseCase,
) : BaseViewModel<NormalChallengerUiState, NormalChallengerEvent>(
    NormalChallengerUiState()
) {
    private var challengerListJob: Job? = null

    fun onSearchKeywordChanged(keyword: String) {
        updateState { copy(searchKeyword = keyword) }
        getChallengers(
            keyword = keyword.trim().takeIf { it.isNotEmpty() },
            debounce = true
        )
    }

    fun refresh() {
        getChallengers()
    }

    fun openPartFilter() {
        updateState { copy(isPartFilterVisible = true) }
    }

    fun dismissPartFilter() {
        updateState { copy(isPartFilterVisible = false) }
    }

    fun selectPartFilter(part: UserPart) {
        updateState { copy(selectedPart = part, isPartFilterVisible = false) }
        getChallengers(selectedPart = part)
    }

    private fun getChallengers(
        keyword: String? = uiState.value.searchKeyword.trim().takeIf { it.isNotEmpty() },
        debounce: Boolean = false,
        selectedPart: UserPart? = uiState.value.selectedPart,
    ) {
        challengerListJob?.cancel()
        challengerListJob = viewModelScope.launch {
            if (debounce) delay(300)
            startLoading()

            val responses = coroutineScope {
                (selectedPart?.let(::listOf)
                    ?: UserPart.entries.filterNot { it == UserPart.UNKNOWN })
                    .map { part ->
                        async {
                            getNormalChallengerListUseCase(
                                cursor = null,
                                size = PAGE_SIZE,
                                schoolId = null,
                                gisuId = null,
                                keyword = keyword,
                                part = part.name
                            )
                        }
                    }
                    .awaitAll()
            }

            val challengers = responses
                .flatMap { response ->
                    var partChallengers = emptyList<UserChallenger>()
                    resultResponse(
                        response = response,
                        successCallback = { partChallengers = it.challengers }
                    )
                    partChallengers
                }
                .distinctBy { it.id }

            updateState {
                copy(sections = challengers.toSections())
            }
            responses.filterIsInstance<ApiState.Fail>().firstOrNull()?.let {
                emitEvent(NormalChallengerEvent.ShowToast(it.failState.message))
            }
            stopLoading()
        }
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
    val selectedPart: UserPart? = null,
    val isPartFilterVisible: Boolean = false,
    val sections: List<NormalChallengerSectionUi> = emptyList(),
    val selectedChallenger: ChallengerInfoDialogModel? = null,
) : UiState

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

private fun List<UserChallenger>.toSections(): List<NormalChallengerSectionUi> {
    return groupBy { it.part }
        .toSortedMap(compareBy<UserPart> { UserPart.entries.indexOf(it) })
        .map { (part, members) ->
            NormalChallengerSectionUi(
                partName = part.label,
                members = members.map { it.toMemberUi() }
            )
        }
}
