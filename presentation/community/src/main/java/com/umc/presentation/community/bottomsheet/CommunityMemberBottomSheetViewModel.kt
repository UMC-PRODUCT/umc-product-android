package com.umc.presentation.community.bottomsheet

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.community.CommunityInvitableMember
import com.umc.domain.usecase.community.GetInvitableCommunityThreadMembersUseCase
import com.umc.domain.usecase.community.InviteCommunityThreadMembersUseCase
import com.umc.presentation.community.model.CommunityInvitableMemberUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@HiltViewModel
class CommunityMemberBottomSheetViewModel @Inject constructor(
    private val getInvitableCommunityThreadMembersUseCase:
    GetInvitableCommunityThreadMembersUseCase,
    private val inviteCommunityThreadMembersUseCase:
    InviteCommunityThreadMembersUseCase,
) : BaseViewModel<
        CommunityMemberBottomSheetState,
        CommunityMemberBottomSheetEvent,
        >(
    CommunityMemberBottomSheetState()
) {

    private var searchJob: Job? = null

    /**
     * 바텀시트를 열 때 호출
     *
     * threadId를 저장한 뒤 초대 가능한 회원의 첫 페이지를 불러옴
     */
    fun initialize(
        threadId: String,
    ) {
        if (threadId.isBlank()) {
            emitEvent(
                CommunityMemberBottomSheetEvent.ShowToast(
                    message = "스레드 정보를 확인할 수 없어요.",
                )
            )
            return
        }

        searchJob?.cancel()

        updateState {
            CommunityMemberBottomSheetState(
                threadId = threadId,
                isLoading = true,
            )
        }

        loadInvitableMembers(
            query = null,
            offset = FIRST_OFFSET,
            append = false,
        )
    }

    /**
     * 검색어가 변경될 때 호출
     *
     * 마지막 입력 후 300ms 동안 추가 입력이 없을 때 검색 API를 호출
     */
    fun searchMembers(
        query: String,
    ) {
        searchJob?.cancel()

        updateState {
            copy(
                query = query,
                isSearching = query.isNotBlank(),
                isLoading = query.isNotBlank(),
                searchResults = if (query.isBlank()) {
                    invitableMembers
                } else {
                    emptyList()
                },
                nextOffset = null,
                hasNext = false,
                errorMessage = null,
            )
        }

        if (query.isBlank()) {
            loadInvitableMembers(
                query = null,
                offset = FIRST_OFFSET,
                append = false,
            )
            return
        }

        searchJob = viewModelScope.launch {
            delay(SEARCH_DELAY)

            loadInvitableMembers(
                query = query.trim(),
                offset = FIRST_OFFSET,
                append = false,
            )
        }
    }

    /**
     * 초대 가능 회원의 다음 페이지를 불러옴
     */
    fun loadMoreMembers() {
        val currentState = uiState.value

        val nextOffset = currentState.nextOffset ?: return

        if (
            currentState.isLoading ||
            currentState.isLoadingMore ||
            !currentState.hasNext
        ) {
            return
        }

        loadInvitableMembers(
            query = currentState.query
                .trim()
                .takeIf { query ->
                    query.isNotBlank()
                },
            offset = nextOffset,
            append = true,
        )
    }

    /**
     * 챌린저를 선택하거나 선택 해제
     */
    fun toggleMember(
        member: CommunityInvitableMemberUiModel,
    ) {
        val currentState = uiState.value

        val isSelected = currentState.selectedMembers.any { selectedMember ->
            selectedMember.memberId == member.memberId
        }

        if (
            !isSelected &&
            currentState.selectedMembers.size >= currentState.maxMemberCount
        ) {
            emitEvent(
                CommunityMemberBottomSheetEvent.ShowToast(
                    message =
                        "챌린저는 최대 ${currentState.maxMemberCount}명까지 추가할 수 있어요.",
                )
            )
            return
        }

        updateState {
            val updatedMembers = if (isSelected) {
                selectedMembers.filterNot { selectedMember ->
                    selectedMember.memberId == member.memberId
                }
            } else {
                selectedMembers + member
            }

            copy(
                selectedMembers = updatedMembers,
            )
        }
    }

    /**
     * 현재 선택된 챌린저를 스레드에 초대
     */
    fun inviteSelectedMembers() {
        val currentState = uiState.value

        if (currentState.isInviting) {
            return
        }

        if (currentState.threadId.isBlank()) {
            emitEvent(
                CommunityMemberBottomSheetEvent.ShowToast(
                    message = "스레드 정보를 확인할 수 없어요.",
                )
            )
            return
        }

        if (currentState.selectedMembers.isEmpty()) {
            emitEvent(
                CommunityMemberBottomSheetEvent.ShowToast(
                    message = "초대할 챌린저를 선택해주세요.",
                )
            )
            return
        }

        /*
         * invitable 응답의 memberId는 String이지만,
         * invite 요청의 memberIds는 List<Long>이므로 Long으로 변환
         */
        val memberIds = currentState.selectedMembers.mapNotNull { member ->
            member.memberId.toLongOrNull()
        }

        if (memberIds.size != currentState.selectedMembers.size) {
            emitEvent(
                CommunityMemberBottomSheetEvent.ShowToast(
                    message = "회원 정보를 처리할 수 없어요.",
                )
            )
            return
        }

        viewModelScope.launch {
            updateState {
                copy(
                    isInviting = true,
                    errorMessage = null,
                )
            }

            inviteCommunityThreadMembersUseCase(
                threadId = currentState.threadId,
                memberIds = memberIds,
            ).onSuccess { invitation ->
                updateState {
                    copy(
                        isInviting = false,
                    )
                }

                emitEvent(
                    CommunityMemberBottomSheetEvent.InviteSuccess(
                        invitedMemberCount =
                            invitation.invitedMembers.size,
                        totalMemberCount =
                            invitation.memberCount,
                    )
                )
            }.onFailure { throwable ->
                updateState {
                    copy(
                        isInviting = false,
                        errorMessage = throwable.message
                            ?: "챌린저를 초대하지 못했어요.",
                    )
                }

                emitEvent(
                    CommunityMemberBottomSheetEvent.ShowToast(
                        message = "챌린저를 초대하지 못했어요.",
                    )
                )
            }
        }
    }

    /**
     * 검색어와 검색 상태만 초기화
     *
     * 이미 선택한 챌린저 목록은 유지
     */
    fun clearSearchOnly() {
        searchJob?.cancel()

        updateState {
            copy(
                query = "",
                isSearching = false,
                searchResults = invitableMembers,
                errorMessage = null,
            )
        }
    }

    /**
     * 바텀시트가 완전히 닫힐 때 전체 상태를 초기화
     */
    fun resetAfterDismiss() {
        searchJob?.cancel()

        updateState {
            CommunityMemberBottomSheetState()
        }
    }

    /**
     * 초대 가능한 챌린저 목록을 조회
     */
    private fun loadInvitableMembers(
        query: String?,
        offset: Int,
        append: Boolean,
    ) {
        val threadId = uiState.value.threadId

        if (threadId.isBlank()) {
            return
        }

        viewModelScope.launch {
            updateState {
                if (append) {
                    copy(
                        isLoadingMore = true,
                        errorMessage = null,
                    )
                } else {
                    copy(
                        isLoading = true,
                        errorMessage = null,
                    )
                }
            }

            getInvitableCommunityThreadMembersUseCase(
                threadId = threadId,
                query = query,
                offset = offset,
                limit = PAGE_SIZE,
            ).onSuccess { page ->
                val newMembers = page.items.map { member ->
                    member.toUiModel()
                }

                updateState {
                    val mergedMembers = if (append) {
                        (searchResults + newMembers)
                            .distinctBy { member ->
                                member.memberId
                            }
                    } else {
                        newMembers
                    }

                    val parsedNextOffset =
                        page.nextOffset?.toIntOrNull()

                    copy(
                        invitableMembers = if (query == null) {
                            mergedMembers
                        } else {
                            invitableMembers
                        },
                        searchResults = mergedMembers,
                        isLoading = false,
                        isLoadingMore = false,
                        nextOffset = parsedNextOffset,
                        hasNext = parsedNextOffset != null,
                        total = page.total,
                        errorMessage = null,
                    )
                }
            }.onFailure { throwable ->
                updateState {
                    copy(
                        isLoading = false,
                        isLoadingMore = false,
                        errorMessage = throwable.message
                            ?: "초대 가능한 챌린저를 불러오지 못했어요.",
                    )
                }
            }
        }
    }

    companion object {
        private const val PAGE_SIZE = 20
        private const val FIRST_OFFSET = 0
        private const val SEARCH_DELAY = 300L
    }
}

private fun CommunityInvitableMember.toUiModel(): CommunityInvitableMemberUiModel {
    return CommunityInvitableMemberUiModel(
        memberId = memberId,
        challengerId = challengerId,
        name = name,
        part = part,
        generation = generation,
    )
}