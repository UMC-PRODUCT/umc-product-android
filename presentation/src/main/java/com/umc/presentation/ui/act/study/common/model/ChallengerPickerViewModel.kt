package com.umc.presentation.ui.act.study.common.picker

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.act.challenger.UserChallenger
import com.umc.domain.repository.AppDataStoreRepository
import com.umc.domain.usecase.challenger.SearchChallengerCursorUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import com.umc.presentation.ui.act.study.common.mapper.toMemberUiModel
import com.umc.presentation.ui.act.study.common.model.MemberUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChallengerPickerViewModel @Inject constructor(
    private val appDataStoreRepository: AppDataStoreRepository,
    private val searchChallengerCursorUseCase: SearchChallengerCursorUseCase
) : BaseViewModel<ChallengerPickerState, ChallengerPickerEvent>(ChallengerPickerState()) {

    private var schoolId: Long = 0L
    private var gisuId: Long? = null

    private var nextCursor: Long? = null
    private var hasNext: Boolean = true
    private var loading = false

    private var searchJob: Job? = null

    init {
        observeScope()
    }

    private fun observeScope() {
        viewModelScope.launch {
            appDataStoreRepository.getUserInfo()
                .distinctUntilChanged { old, new ->
                    old.schoolId == new.schoolId &&
                            old.roles.firstOrNull()?.gisuId == new.roles.firstOrNull()?.gisuId
                }
                .collect { userInfo ->
                    schoolId = userInfo.schoolId
                    gisuId = userInfo.roles.firstOrNull()?.gisuId
                }
        }
    }

    /** 바텀시트 열 때 */
    fun open(part: String?) {
        updateState { copy(part = part) }
        resetAndLoad()
    }

    fun onQueryChanged(q: String) {
        updateState { copy(query = q) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(250)
            resetAndLoad()
        }
    }

    fun loadNext() {
        if (loading || !hasNext) return
        load(cursor = nextCursor, append = true)
    }

    private fun resetAndLoad() {
        nextCursor = null
        hasNext = true
        updateState { copy(items = emptyList(), isEnd = false) }
        load(cursor = null, append = false)
    }

    private fun load(cursor: Long?, append: Boolean) {
        if (schoolId == 0L) return

        viewModelScope.launch {
            loading = true
            updateState { copy(isLoading = true, errorMessage = null) }

            val q = uiState.value.query.trim()
            val part = uiState.value.part

            resultResponse(
                response = searchChallengerCursorUseCase(
                    cursor = cursor,
                    size = 50,
                    schoolId = schoolId,
                    gisuId = gisuId,
                    part = part,
                    name = q.takeIf { it.isNotBlank() },
                    nickname = null
                ),
                successCallback = { data ->
                    nextCursor = data.nextCursor
                    hasNext = data.hasNext

                    val mapped = data.challengers.map { it.toMemberUiModel() }

                    val newItems =
                        if (append) uiState.value.items + mapped
                        else mapped

                    updateState { copy(items = newItems, isLoading = false, isEnd = !hasNext) }
                },
                errorCallback = { fail ->
                    emitEvent(ChallengerPickerEvent.ShowErrorToast(fail.message))
                    updateState { copy(isLoading = false, errorMessage = fail.message) }
                }
            )
            loading = false
        }
    }
}

data class ChallengerPickerState(
    val items: List<MemberUiModel> = emptyList(),
    val query: String = "",
    val part: String? = null,
    val isLoading: Boolean = false,
    val isEnd: Boolean = false,
    val errorMessage: String? = null
) : UiState

sealed interface ChallengerPickerEvent : UiEvent {
    data class ShowErrorToast(val message: String) : ChallengerPickerEvent
}