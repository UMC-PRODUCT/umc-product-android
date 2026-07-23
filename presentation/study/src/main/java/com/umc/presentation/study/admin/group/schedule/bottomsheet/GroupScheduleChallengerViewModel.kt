package com.umc.presentation.study.admin.group.schedule.bottomsheet

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupScheduleChallengerViewModel @Inject constructor() :
    BaseViewModel<GroupScheduleChallengerState, GroupScheduleChallengerEvent>(
        GroupScheduleChallengerState()
    ) {

    private var searchJob: Job? = null

    fun setSelected(list: List<GroupScheduleChallengerUiModel>) {
        updateState {
            copy(
                selectedChallengers = list,
                selectedSummaryText = makeSummaryText(list),
                hasConfirmButton = list.isNotEmpty()
            )
        }
    }

    fun searchChallengers(query: String) {
        searchJob?.cancel()

        if (query.isBlank()) {
            clearSearchOnly()
            return
        }

        updateState {
            copy(
                query = query,
                isSearching = true,
                isLoading = true,
                searchResults = emptyList(),
                hasConfirmButton = true
            )
        }

        searchJob = viewModelScope.launch {
            delay(300)

            val filteredList = uiState.value.allChallengers.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.displayName.contains(query, ignoreCase = true) ||
                        it.partLabel.contains(query, ignoreCase = true) ||
                        it.school.contains(query, ignoreCase = true)
            }

            updateState {
                copy(
                    searchResults = filteredList,
                    isLoading = false
                )
            }
        }
    }

    fun toggleChallenger(item: GroupScheduleChallengerUiModel) {
        updateState {
            val exists = selectedChallengers.any { it.id == item.id }

            val newList = if (exists) {
                selectedChallengers.filterNot { it.id == item.id }
            } else {
                selectedChallengers + item
            }

            copy(
                selectedChallengers = newList,
                selectedSummaryText = makeSummaryText(newList)
            )
        }
    }

    fun clearSearchOnly() {
        searchJob?.cancel()

        updateState {
            copy(
                query = "",
                isSearching = false,
                isLoading = false,
                searchResults = emptyList()
            )
        }
    }

    fun resetAfterConfirm() {
        searchJob?.cancel()

        updateState {
            copy(
                query = "",
                isSearching = false,
                isLoading = false,
                searchResults = emptyList(),
                hasConfirmButton = selectedChallengers.isNotEmpty()
            )
        }
    }

    fun resetAll() {
        searchJob?.cancel()

        updateState {
            copy(
                query = "",
                isSearching = false,
                isLoading = false,
                searchResults = emptyList(),
                hasConfirmButton = false
            )
        }
    }

    private fun makeSummaryText(list: List<GroupScheduleChallengerUiModel>): String {
        return when {
            list.isEmpty() -> ""
            list.size == 1 -> list[0].name
            else -> "${list[0].name} 외 ${list.size - 1}명"
        }
    }
}

data class GroupScheduleChallengerState(
    val selectedChallengers: List<GroupScheduleChallengerUiModel> = emptyList(),
    val selectedSummaryText: String = "",
    val query: String = "",
    val isSearching: Boolean = false,
    val isLoading: Boolean = false,
    val hasConfirmButton: Boolean = false,
    val searchResults: List<GroupScheduleChallengerUiModel> = emptyList(),
    val allChallengers: List<GroupScheduleChallengerUiModel> = listOf(
        GroupScheduleChallengerUiModel(1L, "홍길동", "홍길동/홍종종(11th)", "PM", "학교"),
        GroupScheduleChallengerUiModel(2L, "홍길의", "홍길의/홍철영(10th)", "PM", "학교"),
        GroupScheduleChallengerUiModel(3L, "홍길동", "홍길동/나네임(가수)", "Server", "학교"),
        GroupScheduleChallengerUiModel(4L, "홍길동", "홍길동/나네임(가수)", "Designer", "학교"),
        GroupScheduleChallengerUiModel(5L, "홍길동", "홍길동/나네임(가수)", "iOS", "학교"),
        GroupScheduleChallengerUiModel(6L, "홍길동", "홍길동/나네임(가수)", "Android", "학교")
    )
) : UiState

data class GroupScheduleChallengerUiModel(
    val id: Long,
    val name: String,
    val displayName: String,
    val partLabel: String,
    val school: String,
)

sealed interface GroupScheduleChallengerEvent : UiEvent