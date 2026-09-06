package com.umc.presentation.community.bottomsheet.create

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.home.ParticipantItem
import com.umc.domain.usecase.community.SearchCommunityCreateMembersUseCase
import com.umc.domain.usecase.member.GetMyProfileUseCase
import com.umc.presentation.community.model.CommunityChallengerUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 스레드 생성 시 챌린저 추가 바텀시트의 상태와 로직을 관리하는 ViewModel
 *
 * - 챌린저 검색
 * - 검색 디바운스
 * - 검색 결과 페이지네이션
 * - 챌린저 선택/해제
 * - 최대 선택 인원 제한
 * - 로그인한 본인 검색 결과 제외
 */
@HiltViewModel
class CommunityCreateMemberBottomSheetViewModel @Inject constructor(
    private val searchCommunityCreateMembersUseCase:
    SearchCommunityCreateMembersUseCase,
    private val getMyProfileUseCase:
    GetMyProfileUseCase,
) : BaseViewModel<
        CommunityCreateMemberBottomSheetState,
        CommunityCreateMemberBottomSheetEvent,
        >(
    CommunityCreateMemberBottomSheetState()
) {

    /** 검색 디바운스를 관리하기 위한 Job */
    private var searchJob: Job? = null

    /**
     * 현재 로그인한 사용자의 memberId
     *
     * 스레드 생성자는 챌린저 추가 대상에 포함되지 않도록
     * 검색 결과에서 본인을 제외할 때 사용
     */
    private var myMemberId: Long? = null

    init {
        loadMyProfile()
    }

    /**
     * 현재 로그인한 사용자의 프로필 정보를 조회
     *
     * 조회된 id는 챌린저 검색 결과에서 본인을 제외하기 위해 사용
     */
    private fun loadMyProfile() {
        viewModelScope.launch {
            when (val response = getMyProfileUseCase()) {
                is ApiState.Success -> {
                    myMemberId = response.data.id
                }

                is ApiState.Fail -> Unit
            }
        }
    }

    /**
     * 바텀시트를 열 때 기존에 선택한 챌린저 목록을 전달
     *
     * - 로그인한 본인이 포함되어 있으면 제거
     * - 동일한 memberId의 중복 데이터 제거
     * - 최대 선택 가능 인원까지만 유지
     */
    fun initialize(
        preSelected: List<CommunityChallengerUiModel>,
        maxCount: Int,
    ) {
        searchJob?.cancel()

        updateState {
            CommunityCreateMemberBottomSheetState(
                selectedMembers = preSelected
                    .filterNot { member ->
                        member.memberId == myMemberId
                    }
                    .distinctBy { member ->
                        member.memberId
                    }
                    .take(maxCount)
                    .toImmutableList(),
                maxMemberCount = maxCount,
            )
        }
    }

    /**
     * 검색어 입력 시 300ms 디바운스 후 챌린저 검색 API 호출
     *
     * 검색어가 비어 있는 경우 검색 상태만 초기화하고
     * 기존에 선택된 챌린저 목록은 유지
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
                searchResults = persistentListOf(),
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
     * 검색 결과의 다음 페이지를 조회
     *
     * 현재 로딩 중이거나 다음 페이지가 없으면 요청하지 않음
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
     *
     * - 이미 선택된 챌린저를 누르면 선택 해제
     * - 선택되지 않은 챌린저를 누르면 선택
     * - 최대 선택 가능 인원을 초과하면 Toast 표시
     */
    fun toggleMember(
        member: CommunityChallengerUiModel,
    ) {
        // 혹시 검색 결과 외의 경로로 본인이 전달되더라도 선택 방지
        if (member.memberId == myMemberId) {
            return
        }

        val currentState = uiState.value

        val isSelected = currentState.selectedMembers.any { selectedMember ->
            selectedMember.memberId == member.memberId
        }

        // 최대 인원에 도달한 상태에서 추가 선택하는 경우 제한
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
                selectedMembers = updatedMembers.toImmutableList(),
            )
        }
    }

    /**
     * 검색 관련 상태만 초기화
     *
     * 검색어와 검색 결과는 비우지만
     * 현재 선택된 챌린저 목록은 유지
     */
    fun clearSearchOnly() {
        searchJob?.cancel()

        updateState {
            copy(
                query = "",
                isSearching = false,
                isLoading = false,
                isLoadingMore = false,
                searchResults = persistentListOf(),
                nextCursor = null,
                hasNext = false,
                errorMessage = null,
            )
        }
    }

    /**
     * 바텀시트가 닫힐 때 전체 상태 초기화
     *
     * 진행 중인 검색 Job도 함께 취소
     */
    fun resetAfterDismiss() {
        searchJob?.cancel()

        updateState {
            CommunityCreateMemberBottomSheetState()
        }
    }

    /**
     * 챌린저 검색 API 호출
     *
     * @param keyword 검색할 이름
     * @param cursor 다음 페이지 조회용 cursor
     * @param append true일 경우 기존 검색 결과 뒤에 추가
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

                    val newMembers = page.content
                        // 스레드 생성자인 로그인 사용자는 검색 결과에서 제외
                        .filter { participant ->
                            participant.id != myMemberId
                        }
                        // Domain 모델을 화면용 UI 모델로 변환
                        .map { participant ->
                            participant.toCommunityChallengerUiModel()
                        }

                    updateState {
                        val mergedMembers = if (append) {
                            // 페이지네이션 결과를 기존 결과와 합치고 중복 제거
                            (searchResults + newMembers)
                                .distinctBy { member ->
                                    member.memberId
                                }
                        } else {
                            newMembers
                        }.toImmutableList()

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
        /** 챌린저 검색 API 한 페이지 조회 개수 */
        private const val PAGE_SIZE = 20

        /** 검색 API 호출 전 디바운스 시간 */
        private const val SEARCH_DELAY = 300L
    }
}

/**
 * 챌린저 추가 바텀시트에서 사용하는 UI 상태
 */
data class CommunityCreateMemberBottomSheetState(
    /** 현재 선택된 챌린저 */
    val selectedMembers: ImmutableList<CommunityChallengerUiModel> = persistentListOf(),

    /** 현재 입력된 검색어 */
    val query: String = "",

    /** 검색 화면 여부 */
    val isSearching: Boolean = false,

    /** 최초 검색 결과 로딩 여부 */
    val isLoading: Boolean = false,

    /** 다음 페이지 로딩 여부 */
    val isLoadingMore: Boolean = false,

    /** 챌린저 검색 결과 */
    val searchResults: ImmutableList<CommunityChallengerUiModel> = persistentListOf(),

    /** 다음 페이지 조회용 cursor */
    val nextCursor: Long? = null,

    /** 다음 페이지 존재 여부 */
    val hasNext: Boolean = false,

    /** 선택 가능한 최대 챌린저 수 */
    val maxMemberCount: Int = 8,

    /** 검색 실패 시 표시할 메시지 */
    val errorMessage: String? = null,
) : UiState {

    /** 검색 완료 후 결과가 없는 상태 */
    val isEmpty: Boolean
        get() = isSearching &&
                !isLoading &&
                searchResults.isEmpty()

    /** 챌린저가 한 명 이상 선택되어 있는지 여부 */
    val isConfirmEnabled: Boolean
        get() = selectedMembers.isNotEmpty()

    /** 현재 선택 인원 / 최대 선택 인원 */
    val selectedCountText: String
        get() = "${selectedMembers.size} / $maxMemberCount"
}

/**
 * 챌린저 추가 바텀시트에서 발생하는 일회성 UI 이벤트
 */
sealed interface CommunityCreateMemberBottomSheetEvent : UiEvent {

    /** Toast 메시지 출력 */
    data class ShowToast(
        val message: String,
    ) : CommunityCreateMemberBottomSheetEvent
}

/**
 * 챌린저 검색 API의 ParticipantItem을
 * 커뮤니티 생성 화면에서 사용하는 UI 모델로 변환
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