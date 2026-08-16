package com.umc.presentation.study.admin.group.bottomsheet

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.usecase.challenger.SearchChallengerScheduleUseCase
import com.umc.presentation.study.admin.group.create.AdminStudyGroupCreateMemberUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@HiltViewModel
class AdminStudyGroupMemberEditViewModel @Inject constructor(
    private val searchChallengerScheduleUseCase:
    SearchChallengerScheduleUseCase,
) : BaseViewModel<
        AdminStudyGroupMemberEditState,
        AdminStudyGroupMemberEditEvent,
        >(
    AdminStudyGroupMemberEditState()
) {

    private var searchJob: Job? = null

    /**
     * 바텀시트를 열었을 때 현재 그룹 멤버를 초기값으로 저장
     */
    fun initialize(
        members: List<AdminStudyGroupCreateMemberUiModel>,
    ) {
        searchJob?.cancel()

        updateState {
            copy(
                initialMembers = members,
                selectedMembers = members,
                pendingMembers = emptyList(),
                query = "",
                isSearching = false,
                isLoading = false,
                searchResults = emptyList(),
                nextCursor = null,
                hasNext = true,
            )
        }
    }

    /**
     * 검색어 입력
     */
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
                searchResults = emptyList(),
                pendingMembers = emptyList(),
                nextCursor = null,
                hasNext = true,
            )
        }

        searchJob = viewModelScope.launch {
            delay(500)
            fetchMembers(isNextPage = false)
        }
    }

    private fun fetchMembers(
        isNextPage: Boolean,
    ) {
        val currentState = uiState.value

        if (
            currentState.isLoading &&
            isNextPage
        ) {
            return
        }

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
                    name = currentState.query.ifBlank {
                        null
                    },
                ),
                successCallback = { response ->

                    val mappedMembers =
                        response.content.map { participant ->
                            AdminStudyGroupCreateMemberUiModel(
                                id = participant.id,
                                name = participant.name,
                                displayName = buildString {
                                    append(participant.name)

                                    if (
                                        participant.nickname.isNotBlank()
                                    ) {
                                        append("/")
                                        append(participant.nickname)
                                    }

                                    if (participant.gisu > 0L) {
                                        append("(")
                                        append(participant.gisu)
                                        append("기)")
                                    }
                                },
                                partLabel =
                                    participant.userPart.name,
                                school = participant.school,
                            )
                        }

                    updateState {
                        copy(
                            searchResults =
                                if (isNextPage) {
                                    (
                                            searchResults +
                                                    mappedMembers
                                            ).distinctBy {
                                                member -> member.id
                                        }
                                } else {
                                    mappedMembers.distinctBy {
                                            member -> member.id
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

    /**
     * 검색 결과에서 추가할 멤버 임시 선택
     */
    fun togglePendingMember(
        item: AdminStudyGroupCreateMemberUiModel,
    ) {
        val currentState = uiState.value

        val isAlreadyMember =
            currentState.selectedMembers.any { member ->
                member.id == item.id
            }

        if (isAlreadyMember) {
            return
        }

        updateState {
            val isPending =
                pendingMembers.any { member ->
                    member.id == item.id
                }

            copy(
                pendingMembers =
                    if (isPending) {
                        pendingMembers.filterNot { member ->
                            member.id == item.id
                        }
                    } else {
                        pendingMembers + item
                    }
            )
        }
    }

    /**
     * 검색 화면 확인
     */
    fun confirmPendingMembers() {
        updateState {
            copy(
                selectedMembers =
                    (
                            selectedMembers +
                                    pendingMembers
                            ).distinctBy { member ->
                            member.id
                        },
                pendingMembers = emptyList(),
                query = "",
                isSearching = false,
                isLoading = false,
                searchResults = emptyList(),
                nextCursor = null,
                hasNext = true,
            )
        }
    }

    /**
     * 기존 멤버 삭제
     */
    fun removeMember(
        item: AdminStudyGroupCreateMemberUiModel,
    ) {
        updateState {
            copy(
                selectedMembers =
                    selectedMembers.filterNot { member ->
                        member.id == item.id
                    }
            )
        }
    }

    /**
     * 검색어를 지우면 기존 멤버 화면으로 복귀
     */
    fun clearSearchOnly() {
        searchJob?.cancel()

        updateState {
            copy(
                query = "",
                isSearching = false,
                isLoading = false,
                searchResults = emptyList(),
                pendingMembers = emptyList(),
                nextCursor = null,
                hasNext = true,
            )
        }
    }

    fun loadMoreMembers() {
        val state = uiState.value

        if (
            state.isLoading ||
            !state.hasNext ||
            !state.isSearching
        ) {
            return
        }

        fetchMembers(
            isNextPage = true
        )
    }

    /**
     * 바텀시트를 완전히 닫은 뒤 상태 초기화
     */
    fun reset() {
        searchJob?.cancel()

        updateState {
            AdminStudyGroupMemberEditState()
        }
    }
}

data class AdminStudyGroupMemberEditState(

    // 바텀시트를 처음 열었을 때 서버에서 받은 기존 멤버
    val initialMembers:
    List<AdminStudyGroupCreateMemberUiModel> = emptyList(),

    // 현재 화면에서 유지 중인 멤버
    val selectedMembers:
    List<AdminStudyGroupCreateMemberUiModel> = emptyList(),

    // 검색 화면에서 아직 확인 전인 멤버
    val pendingMembers:
    List<AdminStudyGroupCreateMemberUiModel> = emptyList(),

    val query: String = "",
    val isSearching: Boolean = false,
    val isLoading: Boolean = false,

    val searchResults:
    List<AdminStudyGroupCreateMemberUiModel> = emptyList(),

    val nextCursor: Long? = null,
    val hasNext: Boolean = true,
) : UiState {

    val isConfirmEnabled: Boolean
        get() = pendingMembers.isNotEmpty()

    val hasChanges: Boolean
        get() {
            val initialIds =
                initialMembers
                    .map { member -> member.id }
                    .toSet()

            val currentIds =
                selectedMembers
                    .map { member -> member.id }
                    .toSet()

            return initialIds != currentIds
        }
}

sealed interface AdminStudyGroupMemberEditEvent : UiEvent