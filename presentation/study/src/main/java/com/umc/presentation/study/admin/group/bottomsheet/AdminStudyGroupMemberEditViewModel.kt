package com.umc.presentation.study.admin.group.bottomsheet

import com.umc.component.theme.AppStrings
import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.usecase.challenger.SearchChallengerScheduleUseCase
import com.umc.presentation.study.admin.group.create.AdminStudyGroupCreateMemberUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 관리자 스터디 그룹 멤버 수정 BottomSheet의 상태와 로직을 관리하는 ViewModel
 *
 * 주요 기능
 * - 현재 그룹 멤버 초기화
 * - 챌린저 이름 검색
 * - 검색 결과 커서 페이지네이션
 * - 추가할 멤버 임시 선택
 * - 기존 스터디원 삭제
 * - 변경된 멤버 목록 관리
 */
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

    /** 검색 디바운스 및 이전 검색 취소를 위한 Job */
    private var searchJob: Job? = null

    /**
     * 검색 요청 세대
     *
     * 디바운스만 취소되고 이미 보낸 요청은 끝까지 돌아오므로,
     * 검색어가 바뀌거나 검색을 비운 뒤 도착한 이전 응답은 이 값으로 걸러 버린다.
     */
    private var searchGeneration = 0

    /**
     * 이미 초기화한 기존 멤버 목록
     *
     * 시트는 화면 회전·다크 모드 전환 때마다 다시 그려져 [initialize] 가 다시 불린다.
     * 같은 입력이면 무시해 편집 중이던 멤버와 검색을 지키고, 시트를 닫을 때 [reset] 에서 비운다.
     */
    private var initializedFor: List<AdminStudyGroupCreateMemberUiModel>? = null

    /**
     * BottomSheet를 열었을 때
     * 현재 스터디 그룹에 포함된 멤버를 초기값으로 저장합니다.
     */
    fun initialize(
        members: List<AdminStudyGroupCreateMemberUiModel>,
    ) {
        if (initializedFor == members) return
        initializedFor = members

        searchJob?.cancel()
        searchGeneration++

        updateState {
            copy(
                initialMembers = members.toImmutableList(),
                selectedMembers = members.toImmutableList(),
                pendingMembers = persistentListOf(),
                query = "",
                isSearching = false,
                isLoading = false,
                searchResults = persistentListOf(),
                nextCursor = null,
                hasNext = true,
            )
        }
    }

    /**
     * 챌린저 검색어를 입력합니다.
     *
     * 입력 후 500ms 디바운스를 적용하여
     * 불필요한 검색 API 호출을 방지합니다.
     */
    fun searchMembers(
        query: String,
    ) {
        searchJob?.cancel()
        searchGeneration++

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
                pendingMembers = persistentListOf(),
                nextCursor = null,
                hasNext = true,
            )
        }

        searchJob = viewModelScope.launch {
            delay(500)

            fetchMembers(
                isNextPage = false,
            )
        }
    }

    /**
     * 챌린저 검색 결과를 조회합니다.
     *
     * isNextPage가 true인 경우
     * 기존 검색 결과 뒤에 다음 페이지를 추가합니다.
     */
    private fun fetchMembers(
        isNextPage: Boolean,
    ) {
        val currentState = uiState.value

        // 다음 페이지 로딩 중 중복 요청 방지
        if (
            currentState.isLoading &&
            isNextPage
        ) {
            return
        }

        updateState {
            copy(
                isLoading = true
            )
        }

        // 응답이 돌아왔을 때 그 사이 새 검색이 시작됐는지 확인하기 위해 요청 시점의 세대를 기억한다.
        val generation = searchGeneration

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
                    // 이전 검색어의 늦은 응답이 새 결과를 덮거나 뒤에 붙지 않게 버린다.
                    if (generation != searchGeneration) return@resultResponse

                    /**
                     * 검색 API 결과를
                     * 스터디 그룹에서 사용하는 멤버 UI 모델로 변환합니다.
                     */
                    val mappedMembers =
                        response.content.map { participant ->
                            AdminStudyGroupCreateMemberUiModel(
                                id = participant.id,
                                name = participant.name,

                                // 이름 / 닉네임 / 기수를 조합해 화면에 표시
                                displayName = buildString {
                                    append(participant.name)

                                    if (
                                        participant.nickname.isNotBlank()
                                    ) {
                                        append("/")
                                        append(participant.nickname)
                                    }

                                    if (participant.gisu > 0L) {
                                        append(AppStrings.STUDY_MEMBER_GENERATION_FORMAT.format(participant.gisu))
                                    }
                                },

                                partLabel =
                                    participant.userPart.label,

                                school =
                                    participant.school,

                                // 사용자 프로필 이미지
                                profileImageUrl =
                                    participant.profileImage,
                            )
                        }

                    updateState {
                        copy(
                            searchResults =
                                if (isNextPage) {
                                    (
                                            searchResults +
                                                    mappedMembers
                                            ).distinctBy { member ->
                                            member.id
                                        }.toImmutableList()
                                } else {
                                    mappedMembers.distinctBy { member ->
                                        member.id
                                    }.toImmutableList()
                                },

                            nextCursor =
                                response.nextCursor,

                            hasNext =
                                response.hasNext,

                            isLoading =
                                false,
                        )
                    }
                },
                errorCallback = {
                    if (generation != searchGeneration) return@resultResponse

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
     * 검색 결과에서 추가할 멤버를 임시 선택하거나 해제합니다.
     *
     * 이미 현재 그룹에 포함된 멤버는 다시 선택할 수 없습니다.
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
                        }.toImmutableList()
                    } else {
                        (pendingMembers + item).toImmutableList()
                    }
            )
        }
    }

    /**
     * 검색 화면에서 임시 선택한 멤버를
     * 실제 선택 멤버 목록에 추가합니다.
     */
    fun confirmPendingMembers() {
        searchJob?.cancel()
        searchGeneration++

        updateState {
            copy(
                selectedMembers =
                    (
                            selectedMembers +
                                    pendingMembers
                            ).distinctBy { member ->
                            member.id
                        }.toImmutableList(),

                pendingMembers =
                    persistentListOf(),

                query =
                    "",

                isSearching =
                    false,

                isLoading =
                    false,

                searchResults =
                    persistentListOf(),

                nextCursor =
                    null,

                hasNext =
                    true,
            )
        }
    }

    /**
     * 현재 스터디 그룹 멤버 목록에서
     * 선택한 멤버를 제거합니다.
     */
    fun removeMember(
        item: AdminStudyGroupCreateMemberUiModel,
    ) {
        updateState {
            copy(
                selectedMembers =
                    selectedMembers.filterNot { member ->
                        member.id == item.id
                    }.toImmutableList()
            )
        }
    }

    /**
     * 검색어와 검색 결과만 초기화하고
     * 기존 멤버 목록 화면으로 돌아갑니다.
     */
    fun clearSearchOnly() {
        searchJob?.cancel()
        searchGeneration++

        updateState {
            copy(
                query = "",
                isSearching = false,
                isLoading = false,
                searchResults = persistentListOf(),
                pendingMembers = persistentListOf(),
                nextCursor = null,
                hasNext = true,
            )
        }
    }

    /**
     * 검색 결과의 다음 페이지를 조회합니다.
     */
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
     * BottomSheet를 완전히 닫은 뒤
     * 멤버 수정 상태를 초기화합니다.
     */
    fun reset() {
        searchJob?.cancel()
        searchGeneration++
        initializedFor = null

        updateState {
            AdminStudyGroupMemberEditState()
        }
    }
}

/**
 * 관리자 스터디 그룹 멤버 수정 BottomSheet에서 사용하는 UI 상태
 */
data class AdminStudyGroupMemberEditState(

    /** BottomSheet를 처음 열었을 때 전달받은 기존 멤버 */
    val initialMembers:
    ImmutableList<AdminStudyGroupCreateMemberUiModel> = persistentListOf(),

    /** 현재 수정 화면에서 유지 중인 멤버 */
    val selectedMembers:
    ImmutableList<AdminStudyGroupCreateMemberUiModel> = persistentListOf(),

    /** 검색 화면에서 아직 확인하지 않은 임시 선택 멤버 */
    val pendingMembers:
    ImmutableList<AdminStudyGroupCreateMemberUiModel> = persistentListOf(),

    /** 현재 입력된 검색어 */
    val query: String = "",

    /** 챌린저 검색 화면 여부 */
    val isSearching: Boolean = false,

    /** 검색 결과 로딩 여부 */
    val isLoading: Boolean = false,

    /** 챌린저 검색 결과 */
    val searchResults:
    ImmutableList<AdminStudyGroupCreateMemberUiModel> = persistentListOf(),

    /** 다음 페이지 조회용 cursor */
    val nextCursor: Long? = null,

    /** 다음 페이지 존재 여부 */
    val hasNext: Boolean = true,
) : UiState {

    /** 검색 화면 확인 버튼 활성화 여부 */
    val isConfirmEnabled: Boolean
        get() = pendingMembers.isNotEmpty()

    /**
     * BottomSheet를 처음 열었을 때와 비교하여
     * 스터디 그룹 멤버 구성이 변경됐는지 확인합니다.
     */
    val hasChanges: Boolean
        get() {
            val initialIds =
                initialMembers
                    .map { member ->
                        member.id
                    }
                    .toSet()

            val currentIds =
                selectedMembers
                    .map { member ->
                        member.id
                    }
                    .toSet()

            return initialIds != currentIds
        }
}

/**
 * 관리자 스터디 그룹 멤버 수정 화면의 일회성 UI 이벤트
 */
sealed interface AdminStudyGroupMemberEditEvent : UiEvent