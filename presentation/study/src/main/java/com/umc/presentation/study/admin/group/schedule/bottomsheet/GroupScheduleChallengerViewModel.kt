package com.umc.presentation.study.admin.group.schedule.bottomsheet

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.usecase.challenger.SearchChallengerScheduleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupScheduleChallengerViewModel @Inject constructor(
    private val searchChallengerScheduleUseCase: SearchChallengerScheduleUseCase,
) : BaseViewModel<GroupScheduleChallengerState, GroupScheduleChallengerEvent>(
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
                nextCursor = null,
                hasNext = true,
                hasConfirmButton = true,
            )
        }

        searchJob = viewModelScope.launch {
            delay(300)
            fetchChallengers(isNextPage = false)
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
                searchResults = emptyList(),
                nextCursor = null,
                hasNext = true,
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
                nextCursor = null,
                hasNext = true,
                hasConfirmButton = selectedChallengers.isNotEmpty(),
            )
        }
    }

    fun resetAll() {
        searchJob?.cancel()

        updateState {
            copy(
                selectedChallengers = emptyList(),
                selectedSummaryText = "",
                query = "",
                isSearching = false,
                isLoading = false,
                searchResults = emptyList(),
                nextCursor = null,
                hasNext = true,
                hasConfirmButton = false,
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

    private fun fetchChallengers(
        isNextPage: Boolean,
    ) {
        val currentState = uiState.value

        if (currentState.isLoading && isNextPage) return

        updateState {
            copy(isLoading = true)
        }

        viewModelScope.launch {
            val cursor = if (isNextPage) {
                currentState.nextCursor
            } else {
                null
            }

            resultResponse(
                response = searchChallengerScheduleUseCase(
                    cursor = cursor,
                    size = 50,
                    name = currentState.query.ifBlank { null },
                ),
                successCallback = { response ->
                    val mappedResults = response.content.map { participant ->
                        GroupScheduleChallengerUiModel(
                            id = participant.id,
                            name = participant.name,
                            displayName = buildString {
                                append(participant.name)

                                if (participant.nickname.isNotBlank()) {
                                    append("/")
                                    append(participant.nickname)
                                }

                                if (participant.gisu > 0) {
                                    append("(")
                                    append(participant.gisu)
                                    append("기)")
                                }
                            },
                            partLabel = participant.userPart.name,
                            school = participant.school,
                        )
                    }

                    updateState {
                        copy(
                            searchResults = if (isNextPage) {
                                (searchResults + mappedResults)
                                    .distinctBy { challenger -> challenger.id }
                            } else {
                                mappedResults.distinctBy { challenger ->
                                    challenger.id
                                }
                            },
                            nextCursor = response.nextCursor,
                            hasNext = response.hasNext,
                            isLoading = false,
                        )
                    }
                },
                errorCallback = {
                    updateState {
                        copy(
                            isLoading = false,
                            hasNext = false,
                        )
                    }
                },
            )
        }
    }

    fun loadMoreChallengers() {
        val currentState = uiState.value

        if (
            currentState.isLoading ||
            !currentState.hasNext ||
            !currentState.isSearching
        ) {
            return
        }

        fetchChallengers(isNextPage = true)
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
    val nextCursor: Long? = null,
    val hasNext: Boolean = true,
) : UiState

data class GroupScheduleChallengerUiModel(
    val id: Long,
    val name: String,
    val displayName: String,
    val partLabel: String,
    val school: String,
)

sealed interface GroupScheduleChallengerEvent : UiEvent