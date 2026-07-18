package com.umc.presentation.study.admin.group.create.bottomsheet

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.presentation.study.admin.group.create.AdminStudyGroupCreateMemberUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupCreateMemberPickerViewModel @Inject constructor() :
    BaseViewModel<GroupCreateMemberPickerState, GroupCreateMemberPickerEvent>(
        GroupCreateMemberPickerState()
    ) {

    private var searchJob: Job? = null

    fun setSelected(list: List<AdminStudyGroupCreateMemberUiModel>) {
        updateState {
            copy(selectedMembers = list)
        }
    }

    fun searchMembers(query: String) {
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
                searchResults = emptyList()
            )
        }

        searchJob = viewModelScope.launch {
            delay(300)

            val filteredList = uiState.value.allMembers.filter {
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

    fun toggleMember(item: AdminStudyGroupCreateMemberUiModel) {
        updateState {
            val exists = selectedMembers.any { it.id == item.id }

            val newList = if (exists) {
                selectedMembers.filterNot { it.id == item.id }
            } else {
                selectedMembers + item
            }

            copy(selectedMembers = newList)
        }
    }

    fun addMember(item: AdminStudyGroupCreateMemberUiModel) {
        updateState {
            val exists = selectedMembers.any { it.id == item.id }

            if (exists) {
                this
            } else {
                copy(selectedMembers = selectedMembers + item)
            }
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

    fun resetAfterDismiss() {
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
}

data class GroupCreateMemberPickerState(
    val selectedMembers: List<AdminStudyGroupCreateMemberUiModel> = emptyList(),
    val query: String = "",
    val isSearching: Boolean = false,
    val isLoading: Boolean = false,
    val searchResults: List<AdminStudyGroupCreateMemberUiModel> = emptyList(),
    val allMembers: List<AdminStudyGroupCreateMemberUiModel> = listOf(
        AdminStudyGroupCreateMemberUiModel(1L, "홍길동", "홍길동/홍종종(11th)", "PM", "학교"),
        AdminStudyGroupCreateMemberUiModel(2L, "홍길의", "홍길의/홍철영(10th)", "PM", "학교"),
        AdminStudyGroupCreateMemberUiModel(3L, "홍길동", "홍길동/나네임(가수)", "Server", "학교"),
        AdminStudyGroupCreateMemberUiModel(4L, "홍길동", "홍길동/나네임(가수)", "Design", "학교"),
        AdminStudyGroupCreateMemberUiModel(5L, "홍길동", "홍길동/나네임(가수)", "iOS", "학교"),
        AdminStudyGroupCreateMemberUiModel(6L, "홍길동", "홍길동/나네임(가수)", "Android", "학교")
    )
) : UiState

sealed interface GroupCreateMemberPickerEvent : UiEvent