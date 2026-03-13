package com.umc.presentation.ui.act.challenge

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.act.challenger.ChallengerInfoDialogModel
import com.umc.domain.model.act.challenger.UserChallenger
import com.umc.domain.model.act.challenger.UserPartCount
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.enums.UserPart
import com.umc.domain.repository.AppDataStoreRepository
import com.umc.domain.usecase.attendance.GetChallengerAttendanceHistoryUseCase
import com.umc.domain.usecase.challenger.GetChallengerDetailUseCase
import com.umc.domain.usecase.challenger.GetChallengerListUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserChallengerViewModel @Inject constructor(
    private val appDataStoreRepository: AppDataStoreRepository,
    private val getChallengerListUseCase: GetChallengerListUseCase,
    private val getChallengerDetailUseCase: GetChallengerDetailUseCase,
    private val getChallengerAttendanceHistoryUseCase: GetChallengerAttendanceHistoryUseCase
) : BaseViewModel<UserChallengerUiState, UserChallengerEvent>(UserChallengerUiState()) {

    private var searchJob: Job? = null
    private val partOrder = UserPart.entries.filter { it != UserPart.UNKNOWN } // 순차 로딩할 파트 순서

    init {
        observeUserInfo()
    }

    private fun observeUserInfo() {
        viewModelScope.launch {
            appDataStoreRepository.getUserInfo()
                .distinctUntilChanged { old, new ->
                    old.schoolId == new.schoolId && old.roles.firstOrNull()?.gisuId == new.roles.firstOrNull()?.gisuId
                }
                .collect { userInfo ->
                    updateState { copy(schoolId = userInfo.schoolId, gisuId = userInfo.roles.firstOrNull()?.gisuId, allChallengers = emptyList(), currentPartIndex = 0, nextCursor = null, hasNext = true) }
                    fetchNextPage(isFirstPage = true)
                }
        }
    }

    fun fetchNextPage(isFirstPage: Boolean = false) {
        val currentState = uiState.value

        if (!isFirstPage && (!currentState.hasNext || currentState.isPageLoading)) return

        viewModelScope.launch {
            // 로딩 시작 상태 반영
            updateState { copy(isPageLoading = true) }

            val isSearching = currentState.searchQuery.isNotBlank()
            val requestPart = if (isSearching) null else partOrder.getOrNull(currentState.currentPartIndex)

            resultResponse(
                response = getChallengerListUseCase(
                    cursor = if (isFirstPage) null else currentState.nextCursor,
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
                        val mergedList = if (isFirstPage) data.challengers
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
                        fetchNextPage(isFirstPage = false)
                    }
                },
                errorCallback = { fail ->
                    updateState { copy(isPageLoading = false) }
                    emitEvent(UserChallengerEvent.ShowToast(fail.message, isError = true))
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

    private fun makeChallengerGroups(list: List<UserChallenger>, partCounts: List<UserPartCount>): List<ChallengerGroupUIModel> {
        val groups = UserPart.entries.filter { it != UserPart.UNKNOWN }.mapNotNull { part ->
            val members = list.filter { it.part == part }
            val currentPartCount = partCounts.find { it.part == part } ?: UserPartCount(part, 0)
            if (members.isNotEmpty()) {
                ChallengerGroupUIModel(part, members.mapIndexed { index, c -> UserChallengerUIModel(c, index == members.size - 1) }, currentPartCount)
            } else null
        }
        return groups
    }

    fun navigateToDetail(id: Long) {
        val selectedChallenger = uiState.value.allChallengers.find { it.id == id }
        viewModelScope.launch {
            val detailDeferred = async { getChallengerDetailUseCase(id) }
            val historyDeferred = async { getChallengerAttendanceHistoryUseCase(id) }
            val detailResult = detailDeferred.await()
            val historyResult = historyDeferred.await()

            resultResponse(
                response = detailResult,
                successCallback = { detail ->
                    val historyList = if (historyResult is ApiState.Success) historyResult.data else emptyList()
                    emitEvent(UserChallengerEvent.NavigateToDetail(detail.copy(warningCount = selectedChallenger?.pointSum ?: 0.0, history = historyList)))
                }
            )
        }
    }
}

data class UserChallengerUiState(
    val schoolId: Long = 0,
    val gisuId: Long? = null,
    val currentPartIndex: Int = 0, // 현재 순차 로딩 중인 파트 인덱스
    val allChallengers: List<UserChallenger> = emptyList(),
    val partCounts: List<UserPartCount> = emptyList(),
    val filteredGroups: List<ChallengerGroupUIModel> = emptyList(),
    val searchQuery: String = "",
    val isPageLoading: Boolean = false,
    val nextCursor: Long? = null,
    val hasNext: Boolean = true
) : UiState

sealed interface UserChallengerEvent : UiEvent {
    data class NavigateToDetail(val model: ChallengerInfoDialogModel) : UserChallengerEvent
    data class ShowToast(val message: String, val isError: Boolean = false) : UserChallengerEvent
}