package com.umc.presentation.community.bottomsheet.create

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.component.theme.AppStrings
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
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
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

        val initialMembers = preSelected
            .filterNot { member ->
                member.memberId == myMemberId
            }
            .distinctBy { member ->
                member.memberId
            }
            .take(maxCount)
            .toImmutableList()

        updateState {
            CommunityCreateMemberBottomSheetState(
                selectedMembers = initialMembers,
                initialMembers = initialMembers,
                listedMembers = initialMembers,
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

        // 검색어가 바뀌면 searchMembers 가 이 Job 을 취소해 이전 검색어의 다음 페이지가 붙지 않게 한다.
        searchJob = viewModelScope.launch {
            loadMembers(
                keyword = currentState.query.trim(),
                cursor = nextCursor,
                append = true,
            )
        }
    }

    /**
     * 검색 결과에서 챌린저 선택 또는 선택 해제
     *
     * - 이미 선택된 챌린저를 누르면 선택 해제 (선택 목록에서도 빠짐)
     * - 선택되지 않은 챌린저를 누르면 선택 (선택 목록 끝에 추가)
     * - 이미 최대 인원만큼 선택했다면 추가 선택을 막고 Toast 표시 (멤버 관리 BottomSheet와 동일)
     */
    fun toggleMember(
        member: CommunityChallengerUiModel,
    ) {
        // 혹시 검색 결과 외의 경로로 본인이 전달되더라도 선택 방지
        if (member.memberId == myMemberId) {
            return
        }

        val isSelected = isSelected(member)
        if (!isSelected && !ensureCanSelectMore()) return

        updateState {
            copy(
                selectedMembers = selectedMembers.toggled(member, isSelected),
                listedMembers = if (isSelected) {
                    listedMembers.filterNot { listedMember ->
                        listedMember.memberId == member.memberId
                    }.toImmutableList()
                } else {
                    (listedMembers + member)
                        .distinctBy { listedMember ->
                            listedMember.memberId
                        }
                        .toImmutableList()
                },
            )
        }
    }

    /**
     * 선택 목록의 행에서 챌린저 선택 또는 선택 해제
     *
     * 검색 실패 등으로 검색 결과를 쓸 수 없어도 선택을 뺄 수 있게 하는 경로입니다.
     * 체크를 풀어도 행은 목록에 그대로 남아 다시 체크할 수 있습니다.
     */
    fun toggleListedMember(
        member: CommunityChallengerUiModel,
    ) {
        val isSelected = isSelected(member)
        if (!isSelected && !ensureCanSelectMore()) return

        updateState {
            copy(
                selectedMembers = selectedMembers.toggled(member, isSelected),
            )
        }
    }

    /**
     * 바텀시트를 초대 없이 닫을 때 스레드 생성 화면에 넘길 선택 목록
     *
     * 기존 선택 중 해제한 멤버(선택 목록 행·검색 결과 어느 쪽에서 풀었든)가 있을 때만
     * 그 멤버를 뺀 목록을 반환하고, 그 외에는 null을 반환해 기존 선택을 유지합니다.
     * 검색으로 새로 고른 멤버는 초대 버튼으로만 반영합니다.
     */
    fun membersToApplyOnDismiss(): List<CommunityChallengerUiModel>? {
        val currentState = uiState.value

        val selectedIds = currentState.selectedMembers
            .map { member ->
                member.memberId
            }
            .toSet()

        val remainingMembers = currentState.initialMembers.filter { member ->
            member.memberId in selectedIds
        }

        return remainingMembers.takeIf { members ->
            members.size != currentState.initialMembers.size
        }
    }

    private fun isSelected(
        member: CommunityChallengerUiModel,
    ): Boolean = uiState.value.selectedMembers.any { selectedMember ->
        selectedMember.memberId == member.memberId
    }

    /** 최대 선택 가능 인원에 도달했다면 Toast를 띄우고 false를 반환합니다. (해제는 항상 허용) */
    private fun ensureCanSelectMore(): Boolean {
        val currentState = uiState.value
        if (currentState.selectedMembers.size < currentState.maxMemberCount) return true

        emitEvent(
            CommunityCreateMemberBottomSheetEvent.ShowToast(
                message =
                    AppStrings.COMMUNITY_CREATE_MEMBER_MAX_COUNT_FORMAT.format(currentState.maxMemberCount),
            )
        )
        return false
    }

    private fun ImmutableList<CommunityChallengerUiModel>.toggled(
        member: CommunityChallengerUiModel,
        isSelected: Boolean,
    ): ImmutableList<CommunityChallengerUiModel> {
        val updatedMembers = if (isSelected) {
            filterNot { selectedMember ->
                selectedMember.memberId == member.memberId
            }
        } else {
            this + member
        }

        return updatedMembers
            .distinctBy { selectedMember ->
                selectedMember.memberId
            }
            .toImmutableList()
    }

    /**
     * 초대 버튼 클릭
     *
     * 선택 단계에서 이미 최대 인원을 막지만,
     * 다른 경로로 초과 상태가 되더라도 반영하지 않도록 한 번 더 검사합니다.
     * 통과하면 선택 목록을 스레드 생성 화면에 반영하도록 이벤트를 전달합니다.
     */
    fun onInviteClick() {
        val currentState = uiState.value

        val members = currentState.selectedMembers
            .distinctBy { member ->
                member.memberId
            }

        if (members.size > currentState.maxMemberCount) {
            emitEvent(
                CommunityCreateMemberBottomSheetEvent.ShowToast(
                    message =
                        AppStrings.COMMUNITY_CREATE_MEMBER_MAX_COUNT_FORMAT.format(currentState.maxMemberCount),
                )
            )
            return
        }

        emitEvent(
            CommunityCreateMemberBottomSheetEvent.InviteMembers(
                members = members,
            )
        )
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
     * 호출한 쪽의 searchJob 안에서 실행되므로, 검색어가 바뀌어 Job 이 취소되면
     * 늦게 도착한 이전 검색 결과가 화면을 덮어쓰지 않는다.
     *
     * @param keyword 검색할 이름
     * @param cursor 다음 페이지 조회용 cursor
     * @param append true일 경우 기존 검색 결과 뒤에 추가
     */
    private suspend fun loadMembers(
        keyword: String,
        cursor: Long?,
        append: Boolean,
    ) {
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

        val response = searchCommunityCreateMembersUseCase(
            cursor = cursor,
            size = PAGE_SIZE,
            keyword = keyword,
        )

        // apiCall 이 취소 예외를 Fail 로 바꾸므로, 취소된 조회는 여기서 멈춘다(실패 토스트도 띄우지 않음)
        currentCoroutineContext().ensureActive()

        when (response) {
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
                    // 페이지네이션이면 기존 결과 뒤에 붙인다. 목록 key 가 memberId 라
                    // 같은 회원이 두 번 오면 Compose 가 앱을 종료시키므로 첫 페이지도 중복을 제거한다.
                    val mergedMembers = (if (append) searchResults + newMembers else newMembers)
                        .distinctBy { member ->
                            member.memberId
                        }
                        .toImmutableList()

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
                            AppStrings.COMMUNITY_CREATE_MEMBER_SEARCH_FAILED,
                    )
                }

                emitEvent(
                    CommunityCreateMemberBottomSheetEvent.ShowToast(
                        message = AppStrings.COMMUNITY_CREATE_MEMBER_SEARCH_FAILED,
                    )
                )
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

    /** 바텀시트를 열 때 스레드 생성 화면에서 넘어온 선택 */
    val initialMembers: ImmutableList<CommunityChallengerUiModel> = persistentListOf(),

    /**
     * 검색 중이 아닐 때 선택 목록에 표시할 챌린저
     *
     * 목록 행에서 체크를 풀어도 바로 사라지지 않도록 selectedMembers와 따로 관리합니다.
     */
    val listedMembers: ImmutableList<CommunityChallengerUiModel> = persistentListOf(),

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

    /** 인원 검사를 통과한 선택 목록을 스레드 생성 화면에 반영 */
    data class InviteMembers(
        val members: List<CommunityChallengerUiModel>,
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
        partLabel = userPart.label,
        profileImage = profileImage,
    )
}