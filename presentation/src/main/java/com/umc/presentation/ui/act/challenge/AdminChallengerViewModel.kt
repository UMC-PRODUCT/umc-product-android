package com.umc.presentation.ui.act.challenge

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.act.challenger.AdminChallenger
import com.umc.domain.model.act.challenger.UserPartCount
import com.umc.domain.model.enums.UserPart
import com.umc.domain.repository.AppDataStoreRepository
import com.umc.domain.usecase.challenger.GetAdminChallengerListUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminChallengerViewModel @Inject constructor(
    private val appDataStoreRepository: AppDataStoreRepository,
    private val getAdminChallengerListUseCase: GetAdminChallengerListUseCase
) : BaseViewModel<AdminChallengerUiState, AdminChallengerEvent>(AdminChallengerUiState()) {

    private var searchJob: Job? = null
    private val partOrder = UserPart.entries.filter { it != UserPart.UNKNOWN }

    init {
        observeUserInfo()
    }

    private fun observeUserInfo() {
        viewModelScope.launch {
            appDataStoreRepository.getUserInfo()
                .distinctUntilChanged { old, new ->
                    old.schoolId == new.schoolId &&
                            old.roles.firstOrNull()?.gisuId == new.roles.firstOrNull()?.gisuId
                }
                .collect { userInfo ->
                    updateState {
                        copy(
                            schoolId = userInfo.schoolId,
                            gisuId = userInfo.roles.firstOrNull()?.gisuId,
                            allChallengers = emptyList(),
                            currentPartIndex = 0,
                            nextCursor = null,
                            hasNext = true
                        )
                    }
                    fetchNextPage(isFirstPage = true)
                }
        }
    }

    fun fetchNextPage(isFirstPage: Boolean = false) {
        val currentState = uiState.value
        val isSearchFirstPage =
            currentState.searchQuery.isNotBlank() && currentState.allChallengers.isEmpty()

        if (!isFirstPage && !isSearchFirstPage && (!currentState.hasNext || currentState.isPageLoading)) return

        viewModelScope.launch {
            updateState { copy(isPageLoading = true) }

            val isSearching = currentState.searchQuery.isNotBlank()
            val requestPart = if (isSearching) null else partOrder.getOrNull(currentState.currentPartIndex)

            resultResponse(
                response = getAdminChallengerListUseCase(
                    cursor = if (isFirstPage || isSearchFirstPage) null else currentState.nextCursor,
                    size = 50,
                    schoolId = currentState.schoolId,
                    gisuId = currentState.gisuId,
                    keyword = if (isSearching) currentState.searchQuery else null,
                    part = requestPart?.name
                ),
                successCallback = { data ->
                    val currentPartFinished = !data.hasNext && !isSearching
                    val hasMoreParts = currentState.currentPartIndex < partOrder.size - 1

                    updateState {
                        val mergedList =
                            if (isFirstPage || isSearchFirstPage) data.challengers
                            else (allChallengers + data.challengers).distinctBy { it.id }

                        copy(
                            allChallengers = mergedList,
                            partCounts = data.partCounts,
                            filteredGroups = makeChallengerGroups(mergedList, data.partCounts),
                            nextCursor = if (currentPartFinished && hasMoreParts) null else data.nextCursor,
                            hasNext = if (currentPartFinished) hasMoreParts else data.hasNext,
                            currentPartIndex = if (currentPartFinished && hasMoreParts) currentPartIndex + 1 else currentPartIndex,
                            isPageLoading = false
                        )
                    }

                    if (currentPartFinished && hasMoreParts && data.challengers.isEmpty()) {
                        fetchNextPage(false)
                    }
                },
                errorCallback = { fail ->
                    updateState { copy(isPageLoading = false) }
                    emitEvent(AdminChallengerEvent.ShowToast(fail.message, isError = true))
                }
            )
        }
    }

    fun filterList(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500L)
            updateState {
                copy(
                    searchQuery = query,
                    allChallengers = emptyList(),
                    isPageLoading = false,
                    currentPartIndex = 0,
                    nextCursor = null,
                    hasNext = true
                )
            }
            fetchNextPage(isFirstPage = true)
        }
    }

    private fun makeChallengerGroups(
        list: List<AdminChallenger>,
        partCounts: List<UserPartCount>
    ): List<AdminChallengerGroupUIModel> {
        return UserPart.entries
            .filter { it != UserPart.UNKNOWN }
            .mapNotNull { part ->
                val members = list.filter { it.part == part }
                val currentPartCount = partCounts.find { it.part == part } ?: UserPartCount(part, 0)
                if (members.isNotEmpty()) {
                    AdminChallengerGroupUIModel(part, members, currentPartCount)
                } else null
            }
    }

    fun onChallengerClicked(id: Int) {
        emitEvent(AdminChallengerEvent.NavigateToDetail(id))
    }
}

data class AdminChallengerUiState(
    val schoolId: Long = 0,
    val gisuId: Long? = null,
    val currentPartIndex: Int = 0,
    val allChallengers: List<AdminChallenger> = emptyList(),
    val partCounts: List<UserPartCount> = emptyList(),
    val filteredGroups: List<AdminChallengerGroupUIModel> = emptyList(),
    val searchQuery: String = "",
    val isPageLoading: Boolean = false,
    val nextCursor: Long? = null,
    val hasNext: Boolean = true
) : UiState

sealed interface AdminChallengerEvent : UiEvent {
    data class NavigateToDetail(val challengerId: Int) : AdminChallengerEvent
    data class ShowToast(val message: String, val isError: Boolean = false) : AdminChallengerEvent
}