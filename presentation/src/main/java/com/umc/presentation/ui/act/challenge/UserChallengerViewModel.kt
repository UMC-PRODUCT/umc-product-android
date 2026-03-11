package com.umc.presentation.ui.act.challenge

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.act.challenger.ChallengerInfoDialogModel
import com.umc.domain.model.act.challenger.UserChallenger
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
import kotlinx.coroutines.async
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
                    val currentGisuId = userInfo.roles.firstOrNull()?.gisuId

                    updateState {
                        copy(
                            schoolId = userInfo.schoolId,
                            gisuId = currentGisuId,
                            allChallengers = emptyList(),
                            filteredGroups = emptyList(),
                            nextCursor = null,
                            hasNext = true
                        )
                    }

                    // 초기 데이터 요청 (첫 페이지)
                    fetchNextPage(isFirstPage = true)
                }
        }
    }

    /**
     * 다음 페이지 데이터를 요청하는 페이징 로직
     */
    fun fetchNextPage(isFirstPage: Boolean = false) {
        val state = uiState.value

        // 더 불러올 데이터가 없거나 이미 요청 중이면 중단하여 중복 호출 방지
        if (!isFirstPage && (!state.hasNext || state.isPageLoading)) return

        viewModelScope.launch {
            updateState { copy(isPageLoading = true) }

            val currentCursor = if (isFirstPage) null else state.nextCursor

            val response = getChallengerListUseCase(currentCursor, 50, state.schoolId, state.gisuId)

            when (response) {
                is ApiState.Success -> {
                    val data = response.data
                    val newList = if (isFirstPage) data.challengers else state.allChallengers + data.challengers

                    updateState {
                        copy(
                            allChallengers = newList,
                            filteredGroups = makeChallengerGroups(newList),
                            nextCursor = data.nextCursor,
                            hasNext = data.hasNext,
                            isPageLoading = false
                        )
                    }
                }
                is ApiState.Fail -> {
                    updateState { copy(isPageLoading = false) }
                    emitEvent(UserChallengerEvent.ShowToast(response.failState.message, isError = true))
                }
            }
        }
    }

    /*
    private fun startFetchingAll(schoolId: Long, gisuId: Long?) {
        updateState { copy(allChallengers = emptyList(), filteredGroups = emptyList()) }
        val accumulator = mutableListOf<UserChallenger>()
        fetchAllPages(schoolId, gisuId, null, accumulator)
    }

    private fun fetchAllPages(schoolId: Long, gisuId: Long?, cursor: Long?, accumulator: MutableList<UserChallenger>) {
        viewModelScope.launch {
            resultResponse(
                response = getChallengerListUseCase(cursor, 50, schoolId, gisuId),
                successCallback = { data ->
                    accumulator.addAll(data.challengers)
                    if (data.hasNext) fetchAllPages(schoolId, gisuId, data.nextCursor, accumulator)
                    else updateState { copy(allChallengers = accumulator, filteredGroups = makeChallengerGroups(accumulator)) }
                }
            )
        }
    }
    */

    private fun makeChallengerGroups(list: List<UserChallenger>): List<ChallengerGroupUIModel> {
        return UserPart.entries
            .filter { it != UserPart.UNKNOWN }
            .mapNotNull { part ->
                val members = list.filter { it.part == part }
                if (members.isNotEmpty()) {
                    ChallengerGroupUIModel(
                        part = part,
                        items = members.mapIndexed { index, c ->
                            UserChallengerUIModel(c, index == members.size - 1)
                        }
                    )
                } else null
            }
    }

    fun filterList(query: String) {
        val all = uiState.value.allChallengers
        val filtered = if (query.isBlank()) all else {
            all.filter { it.name.contains(query, true) || it.nickname.contains(query, true) }
        }
        updateState { copy(filteredGroups = makeChallengerGroups(filtered), searchQuery = query) }
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
                    val finalModel = detail.copy(
                        warningCount = selectedChallenger?.pointSum ?: 0.0,
                        history = historyList
                    )
                    emitEvent(UserChallengerEvent.NavigateToDetail(finalModel))
                },
                errorCallback = { failState ->
                    emitEvent(UserChallengerEvent.ShowToast(failState.message, isError = true))
                }
            )
        }
    }
}

data class UserChallengerUiState(
    val schoolId: Long = 0,
    val gisuId: Long? = null,
    val allChallengers: List<UserChallenger> = emptyList(),
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