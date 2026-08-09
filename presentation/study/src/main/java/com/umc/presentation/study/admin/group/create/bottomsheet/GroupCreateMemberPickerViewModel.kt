package com.umc.presentation.study.admin.group.create.bottomsheet

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.usecase.challenger.SearchChallengerScheduleUseCase
import com.umc.presentation.study.admin.group.create.AdminStudyGroupCreateMemberUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupCreateMemberPickerViewModel @Inject constructor(
    private val searchChallengerScheduleUseCase: SearchChallengerScheduleUseCase,
) : BaseViewModel<GroupCreateMemberPickerState, GroupCreateMemberPickerEvent>(
    GroupCreateMemberPickerState()
) {

    private var searchJob: Job? = null

    /**
     * 바텀시트를 처음 열 때 현재 스터디원 목록을 전달
     */
    fun setSelected(
        list: List<AdminStudyGroupCreateMemberUiModel>,
    ) {
        searchJob?.cancel()

        updateState {
            copy(
                selectedMembers = list,
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
     * 검색어가 입력되면 검색 화면으로 전환
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
                    val mappedMembers = response.content.map { participant ->
                        toMemberUiModel(
                            id = participant.id,
                            name = participant.name,
                            nickname = participant.nickname,
                            gisu = participant.gisu,
                            partLabel = participant.userPart.name,
                            school = participant.school,
                        )
                    }

                    updateState {
                        copy(
                            searchResults = if (isNextPage) {
                                (searchResults + mappedMembers)
                                    .distinctBy { member -> member.id }
                            } else {
                                mappedMembers.distinctBy { member -> member.id }
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

    fun loadSelectedMembers(
        memberIds: List<Long>,
    ) {
        searchJob?.cancel()

        if (memberIds.isEmpty()) {
            setSelected(emptyList())
            return
        }

        updateState {
            copy(
                isLoading = true,
                selectedMembers = emptyList(),
                pendingMembers = emptyList(),
                query = "",
                isSearching = false,
                searchResults = emptyList(),
            )
        }

        viewModelScope.launch {
            resultResponse(
                response = searchChallengerScheduleUseCase(
                    cursor = null,
                    size = 50,
                    name = null,
                ),
                successCallback = { response ->
                    val selectedIdSet = memberIds.toSet()

                    val selectedMembers = response.content
                        .filter { participant ->
                            participant.id in selectedIdSet
                        }
                        .map { participant ->
                            toMemberUiModel(
                                id = participant.id,
                                name = participant.name,
                                nickname = participant.nickname,
                                gisu = participant.gisu,
                                partLabel = participant.userPart.name,
                                school = participant.school,
                            )
                        }

                    updateState {
                        copy(
                            selectedMembers = selectedMembers,
                            isLoading = false,
                        )
                    }
                },
                errorCallback = {
                    updateState {
                        copy(isLoading = false)
                    }
                },
            )
        }
    }

    fun loadMoreMembers() {
        val currentState = uiState.value

        if (
            currentState.isLoading ||
            !currentState.hasNext ||
            !currentState.isSearching
        ) {
            return
        }

        fetchMembers(isNextPage = true)
    }

    private fun toMemberUiModel(
        id: Long,
        name: String,
        nickname: String,
        gisu: Long,
        partLabel: String,
        school: String,
    ): AdminStudyGroupCreateMemberUiModel {
        return AdminStudyGroupCreateMemberUiModel(
            id = id,
            name = name,
            displayName = buildString {
                append(name)

                if (nickname.isNotBlank()) {
                    append("/")
                    append(nickname)
                }

                if (gisu > 0L) {
                    append("(")
                    append(gisu)
                    append("기)")
                }
            },
            partLabel = partLabel,
            school = school,
        )
    }

    /**
     * 검색 결과에서 새롭게 추가할 스터디원을 임시 선택
     *
     * 이미 현재 스터디원인 사람은 여기서 제거하거나 다시 추가안함
     */
    fun togglePendingMember(
        item: AdminStudyGroupCreateMemberUiModel,
    ) {
        val currentState = uiState.value

        val isAlreadyMember = currentState.selectedMembers.any { member ->
            member.id == item.id
        }

        if (isAlreadyMember) return

        updateState {
            val isPending = pendingMembers.any { member ->
                member.id == item.id
            }

            copy(
                pendingMembers = if (isPending) {
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
     * 검색 화면의 확인 버튼을 누르면 임시 선택 인원을 기존 목록에 추가하고
     * 현재 스터디원 화면으로 돌아가기
     */
    fun confirmPendingMembers() {
        updateState {
            copy(
                selectedMembers = (selectedMembers + pendingMembers)
                    .distinctBy { member -> member.id },
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
     * 현재 스터디원 목록에서 삭제
     */
    fun removeMember(
        item: AdminStudyGroupCreateMemberUiModel,
    ) {
        updateState {
            copy(
                selectedMembers = selectedMembers.filterNot { member ->
                    member.id == item.id
                }
            )
        }
    }

    /**
     * 검색어를 모두 지웠을 때 임시 선택을 취소하고 현재 목록으로 돌아가기
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

    /**
     * 바텀시트가 닫힌 뒤 내부 상태를 초기화
     */
    fun resetAfterDismiss() {
        searchJob?.cancel()

        updateState {
            GroupCreateMemberPickerState()
        }
    }
}

data class GroupCreateMemberPickerState(
    // 현재 스터디원 목록
    val selectedMembers: List<AdminStudyGroupCreateMemberUiModel> = emptyList(),

    // 검색 화면에서 아직 확인하지 않은 임시 선택 목록
    val pendingMembers: List<AdminStudyGroupCreateMemberUiModel> = emptyList(),

    val query: String = "",
    val isSearching: Boolean = false,
    val isLoading: Boolean = false,

    val searchResults: List<AdminStudyGroupCreateMemberUiModel> = emptyList(),

    val nextCursor: Long? = null,
    val hasNext: Boolean = true,
) : UiState {

    val isConfirmEnabled: Boolean
        get() = pendingMembers.isNotEmpty()
}

sealed interface GroupCreateMemberPickerEvent : UiEvent