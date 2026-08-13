package com.umc.presentation.community.create.bottomsheet

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.home.ParticipantItem
import com.umc.domain.usecase.community.SearchCommunityCreateMembersUseCase
import com.umc.presentation.community.model.CommunityChallengerUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@HiltViewModel
class CommunityCreateMemberBottomSheetViewModel @Inject constructor(
    private val searchCommunityCreateMembersUseCase:
    SearchCommunityCreateMembersUseCase,
) : BaseViewModel<
        CommunityCreateMemberBottomSheetState,
        CommunityCreateMemberBottomSheetEvent,
        >(
    CommunityCreateMemberBottomSheetState()
) {

    private var searchJob: Job? = null

    /**
     * 바텀시트를 열 때 기존에 선택한 챌린저를 전달
     */
    fun initialize(
        preSelected: List<CommunityChallengerUiModel>,
        maxCount: Int,
    ) {
        searchJob?.cancel()

        updateState {
            CommunityCreateMemberBottomSheetState(
                selectedMembers = preSelected
                    .distinctBy { member ->
                        member.memberId
                    }
                    .take(maxCount),
                maxMemberCount = maxCount,
            )
        }
    }

    /**
     * 검색어 입력 시 300ms 디바운스 후 챌린저 검색 API를 호출
     */
    fun searchMembers(
        query: String,
    ) {
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
                nextCursor = null,
                hasNext = false,
                errorMessage = null,
            )
        }

        searchJob = viewModelScope.launch {
            delay(SEARCH_DELAY)

            loadMembers(
                keyword = query.trim(),
                cursor = null,
                append = false,
            )
        }
    }

    /**
     * 검색 결과의 다음 페이지를 불러옴
     */
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
     * 챌린저 선택 또는 선택 해제
     */
    fun toggleMember(
        member: CommunityChallengerUiModel,
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
                CommunityCreateMemberBottomSheetEvent.ShowToast(
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
     * 검색어와 검색 결과만 초기화
     * 선택된 챌린저는 유지
     */
    fun clearSearchOnly() {
        searchJob?.cancel()

        updateState {
            copy(
                query = "",
                isSearching = false,
                isLoading = false,
                isLoadingMore = false,
                searchResults = emptyList(),
                nextCursor = null,
                hasNext = false,
                errorMessage = null,
            )
        }
    }

    /**
     * 바텀시트가 닫힐 때 전체 상태를 초기화
     */
    fun resetAfterDismiss() {
        searchJob?.cancel()

        updateState {
            CommunityCreateMemberBottomSheetState()
        }
    }

    /**
     * 챌린저 검색 API 호출
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

                    val newMembers = page.content.map { participant ->
                        participant.toCommunityChallengerUiModel()
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
                        CommunityCreateMemberBottomSheetEvent.ShowToast(
                            message = "챌린저 검색 결과를 불러오지 못했어요.",
                        )
                    )
                }
            }
        }
    }

    companion object {
        private const val PAGE_SIZE = 20
        private const val SEARCH_DELAY = 300L
        private const val DEFAULT_MAX_MEMBER_COUNT = 8
    }
}

data class CommunityCreateMemberBottomSheetState(
    val selectedMembers: List<CommunityChallengerUiModel> = emptyList(),

    val query: String = "",
    val isSearching: Boolean = false,

    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,

    val searchResults: List<CommunityChallengerUiModel> = emptyList(),

    val nextCursor: Long? = null,
    val hasNext: Boolean = false,

    val maxMemberCount: Int = 8,

    val errorMessage: String? = null,
) : UiState {

    val isEmpty: Boolean
        get() = isSearching &&
                !isLoading &&
                searchResults.isEmpty()

    val isConfirmEnabled: Boolean
        get() = selectedMembers.isNotEmpty()

    val selectedCountText: String
        get() = "${selectedMembers.size} / $maxMemberCount"
}

sealed interface CommunityCreateMemberBottomSheetEvent : UiEvent {

    data class ShowToast(
        val message: String,
    ) : CommunityCreateMemberBottomSheetEvent
}

/**
 * 기존 챌린저 검색 결과를 커뮤니티 생성 화면 모델로 변환
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