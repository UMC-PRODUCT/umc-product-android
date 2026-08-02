package com.umc.presentation.community.bottomsheet

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.community.CommunityThreadMember
import com.umc.domain.model.home.ParticipantItem
import com.umc.domain.usecase.community.GetCommunityThreadMembersUseCase
import com.umc.domain.usecase.community.InviteCommunityThreadMembersUseCase
import com.umc.domain.usecase.community.KickCommunityThreadMemberUseCase
import com.umc.domain.usecase.community.SearchCommunityCreateMembersUseCase
import com.umc.presentation.community.model.CommunityChallengerUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@HiltViewModel
class CommunityMemberBottomSheetViewModel @Inject constructor(
    private val getCommunityThreadMembersUseCase:
    GetCommunityThreadMembersUseCase,
    private val searchCommunityCreateMembersUseCase:
    SearchCommunityCreateMembersUseCase,
    private val inviteCommunityThreadMembersUseCase:
    InviteCommunityThreadMembersUseCase,
    private val kickCommunityThreadMemberUseCase:
    KickCommunityThreadMemberUseCase,
) : BaseViewModel<
        CommunityMemberBottomSheetState,
        CommunityMemberBottomSheetEvent,
        >(
    CommunityMemberBottomSheetState()
) {

    private var searchJob: Job? = null

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

        loadCurrentMembers(
            threadId = threadId,
        )
    }

    /**
     * 스레드에 현재 참여 중인 멤버를 조회한 뒤
     * 챌린저 검색 결과와 memberId로 매칭
     */
    private fun loadCurrentMembers(
        threadId: String,
    ) {
        viewModelScope.launch {
            val memberPage = getCommunityThreadMembersUseCase(
                threadId = threadId,
                query = null,
                role = null,
                part = null,
                generation = null,
                offset = FIRST_OFFSET,
                limit = CURRENT_MEMBER_PAGE_SIZE,
            ).getOrElse { throwable ->
                updateState {
                    copy(
                        isLoading = false,
                        errorMessage = throwable.message
                            ?: "현재 스레드 멤버를 불러오지 못했어요.",
                    )
                }
                return@launch
            }

            val currentMembers = memberPage.items.map { threadMember ->
                findChallengerDetail(
                    threadMember = threadMember,
                )
            }

            updateState {
                copy(
                    currentMembers = currentMembers,
                    selectedMembers = currentMembers,
                    isLoading = false,
                    errorMessage = null,
                )
            }
        }
    }

    /**
     * 기존 멤버의 이름으로 챌린저 검색 API를 호출한 뒤,
     * memberId가 정확히 일치하는 결과를 가져옴
     *
     * 매칭에 실패하면 기존 멤버 응답 정보로 기본 UI 모델을 만듬
     */
    private suspend fun findChallengerDetail(
        threadMember: CommunityThreadMember,
    ): CommunityChallengerUiModel {
        val targetMemberId =
            threadMember.memberId.toLongOrNull()

        if (targetMemberId == null) {
            return threadMember.toFallbackUiModel()
        }

        return when (
            val response = searchCommunityCreateMembersUseCase(
                cursor = null,
                size = MEMBER_MATCH_PAGE_SIZE,
                keyword = threadMember.name,
            )
        ) {
            is ApiState.Success -> {
                response.data.content
                    .firstOrNull { participant ->
                        participant.id == targetMemberId
                    }
                    ?.toCommunityChallengerUiModel()
                    ?: threadMember.toFallbackUiModel()
            }

            is ApiState.Fail -> {
                threadMember.toFallbackUiModel()
            }
        }
    }

    fun searchMembers(
        query: String,
    ) {
        searchJob?.cancel()

        val trimmedQuery = query.trim()
        val currentState = uiState.value

        if (trimmedQuery.isBlank()) {
            clearSearchOnly()
            return
        }

        updateState {
            copy(
                query = query,
                isSearching = true,

                // 검색 화면에 처음 진입할 때
                // 현재 멤버 전원을 체크 상태로 설정
                selectedMembers = if (!currentState.isSearching) {
                    currentMembers
                } else {
                    selectedMembers
                },

                isLoading = true,
                isLoadingMore = false,
                searchResults = emptyList(),
                nextCursor = null,
                hasNext = false,
                errorMessage = null,
            )
        }

        searchJob = viewModelScope.launch {
            delay(SEARCH_DELAY)

            loadMembers(
                keyword = trimmedQuery,
                cursor = null,
                append = false,
            )
        }
    }

    fun loadMoreMembers() {
        val currentState = uiState.value
        val nextCursor = currentState.nextCursor ?: return

        if (
            currentState.isLoading ||
            currentState.isLoadingMore ||
            !currentState.hasNext ||
            currentState.query.isBlank()
        ) {
            return
        }

        loadMembers(
            keyword = currentState.query.trim(),
            cursor = nextCursor,
            append = true,
        )
    }

    /**
     * 생성 바텀시트와 같은 챌린저 검색 API를 사용
     *
     * 현재 멤버도 검색 결과에 포함되며,
     * selectedMembers에 들어 있는 멤버는 체크 상태로 표시
     */
    private fun loadMembers(
        keyword: String,
        cursor: Long?,
        append: Boolean,
    ) {
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

            when (
                val response = searchCommunityCreateMembersUseCase(
                    cursor = cursor,
                    size = PAGE_SIZE,
                    keyword = keyword,
                )
            ) {
                is ApiState.Success -> {
                    val page = response.data

                    val searchedMembers = page.content.map { participant ->
                        participant.toCommunityChallengerUiModel()
                    }

                    updateState {
                        val mergedMembers = if (append) {
                            (searchResults + searchedMembers)
                                .distinctBy { member ->
                                    member.memberId
                                }
                        } else {
                            searchedMembers
                        }

                        copy(
                            searchResults = mergedMembers,
                            isLoading = false,
                            isLoadingMore = false,
                            nextCursor = page.nextCursor,
                            hasNext = page.hasNext,
                            errorMessage = null,
                        )
                    }
                }

                is ApiState.Fail -> {
                    updateState {
                        copy(
                            isLoading = false,
                            isLoadingMore = false,
                            errorMessage =
                                "챌린저 검색 결과를 불러오지 못했어요.",
                        )
                    }

                    emitEvent(
                        CommunityMemberBottomSheetEvent.ShowToast(
                            message =
                                "챌린저 검색 결과를 불러오지 못했어요.",
                        )
                    )
                }
            }
        }
    }

    fun toggleMember(
        member: CommunityChallengerUiModel,
    ) {
        val currentState = uiState.value

        val isSelected =
            currentState.selectedMembers.any { selectedMember ->
                selectedMember.memberId == member.memberId
            }

        if (
            !isSelected &&
            currentState.selectedMembers.size >=
            currentState.maxMemberCount
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
                selectedMembers = updatedMembers
                    .distinctBy { selectedMember ->
                        selectedMember.memberId
                    },
            )
        }
    }

    /**
     * 확인 버튼을 눌렀을 때 현재 멤버와 최종 선택 멤버를 비교
     *
     * 새로 선택된 멤버는 invite,
     * 선택 해제된 기존 멤버는 kick 처리
     */
    fun updateMembers() {
        val currentState = uiState.value

        if (currentState.isUpdatingMembers) {
            return
        }

        if (!currentState.hasMemberChanges) {
            clearSearchOnly()
            return
        }

        val addedMembers = currentState.addedMembers
        val removedMembers = currentState.removedMembers

        viewModelScope.launch {
            updateState {
                copy(
                    isUpdatingMembers = true,
                    errorMessage = null,
                )
            }

            val result = runCatching {
                if (addedMembers.isNotEmpty()) {
                    inviteCommunityThreadMembersUseCase(
                        threadId = currentState.threadId,
                        memberIds = addedMembers.map { member ->
                            member.memberId
                        },
                    ).getOrThrow()
                }

                removedMembers.forEach { member ->
                    kickCommunityThreadMemberUseCase(
                        threadId = currentState.threadId,
                        memberId = member.memberId.toString(),
                    ).getOrThrow()
                }
            }

            result.onSuccess {
                val updatedCurrentMembers =
                    currentState.selectedMembers
                        .distinctBy { member ->
                            member.memberId
                        }

                updateState {
                    copy(
                        currentMembers = updatedCurrentMembers,
                        selectedMembers = updatedCurrentMembers,

                        query = "",
                        isSearching = false,
                        isLoading = false,
                        isLoadingMore = false,
                        isUpdatingMembers = false,

                        searchResults = emptyList(),
                        nextCursor = null,
                        hasNext = false,
                        errorMessage = null,
                    )
                }

                emitEvent(
                    CommunityMemberBottomSheetEvent.MemberUpdateSuccess(
                        addedMemberCount = addedMembers.size,
                        removedMemberCount = removedMembers.size,
                    )
                )
            }.onFailure { throwable ->
                updateState {
                    copy(
                        isUpdatingMembers = false,
                        errorMessage = throwable.message
                            ?: "스레드 멤버를 변경하지 못했어요.",
                    )
                }

                emitEvent(
                    CommunityMemberBottomSheetEvent.ShowToast(
                        message = throwable.message
                            ?: "스레드 멤버를 변경하지 못했어요.",
                    )
                )
            }
        }
    }

    /**
     * 현재 멤버 화면의 삭제 버튼
     */
    fun kickMember(
        member: CommunityChallengerUiModel,
    ) {
        val currentState = uiState.value

        if (currentState.deletingMemberId != null) {
            return
        }

        viewModelScope.launch {
            updateState {
                copy(
                    deletingMemberId = member.memberId,
                    errorMessage = null,
                )
            }

            kickCommunityThreadMemberUseCase(
                threadId = currentState.threadId,
                memberId = member.memberId.toString(),
            ).onSuccess {
                updateState {
                    copy(
                        currentMembers =
                            currentMembers.filterNot { currentMember ->
                                currentMember.memberId == member.memberId
                            },
                        selectedMembers =
                            selectedMembers.filterNot { selectedMember ->
                                selectedMember.memberId == member.memberId
                            },
                        deletingMemberId = null,
                    )
                }

                emitEvent(
                    CommunityMemberBottomSheetEvent.MemberKickSuccess(
                        memberId = member.memberId,
                    )
                )
            }.onFailure { throwable ->
                updateState {
                    copy(
                        deletingMemberId = null,
                        errorMessage = throwable.message
                            ?: "멤버를 삭제하지 못했어요.",
                    )
                }

                emitEvent(
                    CommunityMemberBottomSheetEvent.ShowToast(
                        message = throwable.message
                            ?: "멤버를 삭제하지 못했어요.",
                    )
                )
            }
        }
    }

    fun clearSearchOnly() {
        searchJob?.cancel()

        updateState {
            copy(
                query = "",
                isSearching = false,
                selectedMembers = currentMembers,
                searchResults = emptyList(),
                isLoading = false,
                isLoadingMore = false,
                nextCursor = null,
                hasNext = false,
                errorMessage = null,
            )
        }
    }

    fun resetAfterDismiss() {
        searchJob?.cancel()

        updateState {
            CommunityMemberBottomSheetState()
        }
    }

    companion object {
        private const val PAGE_SIZE = 20
        private const val MEMBER_MATCH_PAGE_SIZE = 100
        private const val CURRENT_MEMBER_PAGE_SIZE = 100
        private const val FIRST_OFFSET = 0
        private const val SEARCH_DELAY = 300L
    }
}

/**
 * 챌린저 검색 결과를 수정 바텀시트 UI 모델로 변환
 */
private fun ParticipantItem.toCommunityChallengerUiModel():
        CommunityChallengerUiModel {
    return CommunityChallengerUiModel(
        memberId = id,
        name = name,
        nickname = nickname,
        school = school,
        generation = gisu,
        partLabel = userPart.name,
        profileImage = profileImage,
    )
}

/**
 * 챌린저 상세 매칭에 실패했을 때 사용하는 기본 모델
 */
private fun CommunityThreadMember.toFallbackUiModel():
        CommunityChallengerUiModel {
    return CommunityChallengerUiModel(
        memberId = memberId.toLongOrNull() ?: 0L,
        name = name,
        nickname = "",
        school = "",
        generation = generation.toLongOrNull() ?: 0L,
        partLabel = part,
        profileImage = "",
    )
}