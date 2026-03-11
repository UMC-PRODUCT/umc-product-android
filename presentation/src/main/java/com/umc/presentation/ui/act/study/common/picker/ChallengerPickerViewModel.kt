package com.umc.presentation.ui.act.study.common.picker

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.act.challenger.UserChallenger
import com.umc.domain.usecase.challenger.SearchChallengerCursorUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import com.umc.presentation.ui.act.study.common.mapper.toMemberUiModel
import com.umc.presentation.ui.act.study.common.model.MemberUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChallengerPickerViewModel @Inject constructor(
    private val searchChallengerCursorUseCase: SearchChallengerCursorUseCase
) : BaseViewModel<ChallengerPickerState, ChallengerPickerEvent>(ChallengerPickerState()) {

    private var nextCursor: Long? = null
    private var hasNext: Boolean = true
    private var loading = false
    private var searchJob: Job? = null

    fun open(part: String?, selectedSchoolId: Long? = null, selectedGisuId: Long? = null) {
        updateState { copy(part = part, selectedSchoolId = selectedSchoolId, selectedGisuId = selectedGisuId) }
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
        if (loading) return

        viewModelScope.launch {
            loading = true
            try {
                updateState { copy(isLoading = true, errorMessage = null) }

                val state = uiState.value
                val q = state.query.trim()

                resultResponse(
                    response = searchChallengerCursorUseCase(
                        cursor = cursor,
                        size = 50,
                        schoolId = state.selectedSchoolId,
                        gisuId = state.selectedGisuId,
                        part = state.part,
                        keyword = q.takeIf { it.isNotBlank() },
                    ),
                    successCallback = { data ->
                        nextCursor = data.nextCursor
                        hasNext = data.hasNext

                        val mapped = data.challengers.map(UserChallenger::toMemberUiModel)
                        val newItems =
                            if (append) uiState.value.items + mapped else mapped

                        updateState { copy(items = newItems, isLoading = false, isEnd = !hasNext) }
                    },
                    errorCallback = { fail ->
                        emitEvent(ChallengerPickerEvent.ShowErrorToast(fail.message))
                        updateState { copy(isLoading = false, errorMessage = fail.message) }
                    }
                )
            } finally {
                loading = false
            }
        }
    }
}

data class ChallengerPickerState(
    val items: List<MemberUiModel> = emptyList(),
    val query: String = "",
    val part: String? = null,
    val selectedSchoolId: Long? = null,
    val selectedGisuId: Long? = null,
    val isLoading: Boolean = false,
    val isEnd: Boolean = false,
    val errorMessage: String? = null
) : UiState

sealed interface ChallengerPickerEvent : UiEvent {
    data class ShowErrorToast(val message: String) : ChallengerPickerEvent
}