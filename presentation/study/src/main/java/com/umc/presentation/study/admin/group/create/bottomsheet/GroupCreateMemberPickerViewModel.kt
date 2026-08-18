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

/**
 * 스터디 그룹 생성 화면의 멤버 선택 상태를 관리하는 ViewModel
 *
 * 주요 기능
 * - 기존 선택 멤버 초기화
 * - 챌린저 이름 검색
 * - 검색 결과 페이지네이션
 * - 검색 결과 임시 선택
 * - 선택 멤버 추가 및 삭제
 * - 프로필 이미지 정보를 UI 모델에 매핑
 */
@HiltViewModel
class GroupCreateMemberPickerViewModel @Inject constructor(
    private val searchChallengerScheduleUseCase: SearchChallengerScheduleUseCase,
) : BaseViewModel<
        GroupCreateMemberPickerState,
        GroupCreateMemberPickerEvent,
        >(
    GroupCreateMemberPickerState()
) {

    /**
     * 검색 debounce 처리를 위한 Job
     *
     * 새로운 검색어가 입력되면 이전 검색 요청을 취소합니다.
     */
    private var searchJob: Job? = null

    /**
     * 바텀시트를 처음 열 때 현재 선택된 멤버 목록을 설정합니다.
     *
     * 생성 화면에서 이미 선택되어 있던 스터디원이나
     * 파트장 정보를 그대로 유지하기 위해 사용합니다.
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
     * 검색어 입력 시 챌린저 검색을 시작합니다.
     *
     * 검색어가 비어 있으면 검색 상태를 종료하고
     * 기존 선택 멤버 목록 화면으로 돌아갑니다.
     *
     * 연속 입력 시 불필요한 API 호출을 줄이기 위해
     * 500ms debounce를 적용합니다.
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
                pendingMembers = emptyList(),
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
     * 챌린저 검색 API를 호출합니다.
     *
     * @param isNextPage
     * false이면 새로운 검색,
     * true이면 현재 검색 결과의 다음 페이지를 조회합니다.
     */
    private fun fetchMembers(
        isNextPage: Boolean,
    ) {
        val currentState = uiState.value

        // 다음 페이지 로딩 중 중복 호출 방지
        if (
            currentState.isLoading &&
            isNextPage
        ) {
            return
        }

        updateState {
            copy(
                isLoading = true,
            )
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

                    // API 응답을 스터디 그룹 멤버 UI 모델로 변환
                    val mappedMembers = response.content.map { participant ->
                        toMemberUiModel(
                            id = participant.id,
                            name = participant.name,
                            nickname = participant.nickname,
                            gisu = participant.gisu,
                            partLabel = participant.userPart.name,
                            school = participant.school,
                            profileImageUrl = participant.profileImage,
                        )
                    }

                    updateState {
                        copy(
                            searchResults = if (isNextPage) {
                                (
                                        searchResults +
                                                mappedMembers
                                        )
                                    .distinctBy { member ->
                                        member.id
                                    }
                            } else {
                                mappedMembers.distinctBy { member ->
                                    member.id
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
     * 기존에 선택되어 있던 멤버 ID를 기준으로
     * 실제 챌린저 정보를 다시 조회합니다.
     *
     * 기존 멤버 정보에 프로필 이미지 등의 정보가 없는 경우
     * API 응답과 매칭하여 완전한 UI 모델을 생성하기 위해 사용합니다.
     */
    fun loadSelectedMembers(
        memberIds: List<Long>,
    ) {
        searchJob?.cancel()

        if (memberIds.isEmpty()) {
            setSelected(
                emptyList(),
            )
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
                                profileImageUrl = participant.profileImage,
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
                        copy(
                            isLoading = false,
                        )
                    }
                },
            )
        }
    }

    /**
     * 현재 검색 결과의 다음 페이지를 조회합니다.
     *
     * 검색 중이 아니거나,
     * 이미 로딩 중이거나,
     * 다음 페이지가 없는 경우에는 요청하지 않습니다.
     */
    fun loadMoreMembers() {
        val currentState = uiState.value

        if (
            currentState.isLoading ||
            !currentState.hasNext ||
            !currentState.isSearching
        ) {
            return
        }

        fetchMembers(
            isNextPage = true,
        )
    }

    /**
     * 검색 API의 챌린저 정보를
     * 스터디 그룹 생성 화면에서 사용하는 UI 모델로 변환합니다.
     *
     * 이름 / 닉네임 / 기수를 조합하여 displayName을 생성하고,
     * 프로필 이미지 URL도 함께 전달합니다.
     */
    private fun toMemberUiModel(
        id: Long,
        name: String,
        nickname: String,
        gisu: Long,
        partLabel: String,
        school: String,
        profileImageUrl: String?,
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
            profileImageUrl = profileImageUrl,
        )
    }

    /**
     * 검색 결과에서 새롭게 추가할 멤버를 임시 선택합니다.
     *
     * 이미 현재 그룹에 포함되어 있는 멤버는
     * 다시 선택할 수 없습니다.
     *
     * 아직 확인하지 않은 멤버는 pendingMembers에서 관리합니다.
     */
    fun togglePendingMember(
        item: AdminStudyGroupCreateMemberUiModel,
    ) {
        val currentState = uiState.value

        val isAlreadyMember = currentState.selectedMembers.any { member ->
            member.id == item.id
        }

        if (isAlreadyMember) {
            return
        }

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
     * 검색 화면에서 선택한 임시 멤버를
     * 현재 선택된 멤버 목록에 최종 추가합니다.
     *
     * 추가 후 검색 상태를 초기화하고
     * 현재 선택 멤버 목록 화면으로 돌아갑니다.
     */
    fun confirmPendingMembers() {
        updateState {
            copy(
                selectedMembers = (
                        selectedMembers +
                                pendingMembers
                        )
                    .distinctBy { member ->
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
     * 현재 선택된 멤버 목록에서 특정 멤버를 제거합니다.
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
     * 검색어가 모두 지워졌을 때 검색 상태만 초기화합니다.
     *
     * 이미 선택되어 있는 멤버 목록은 유지하며,
     * 검색 중 임시 선택했던 pendingMembers는 초기화합니다.
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
     * 바텀시트가 완전히 닫힌 후
     * ViewModel 내부 상태를 초기 상태로 되돌립니다.
     */
    fun resetAfterDismiss() {
        searchJob?.cancel()

        updateState {
            GroupCreateMemberPickerState()
        }
    }
}

/**
 * 스터디 그룹 멤버 선택 화면의 UI 상태
 */
data class GroupCreateMemberPickerState(

    // 현재 최종 선택되어 있는 멤버 목록
    val selectedMembers:
    List<AdminStudyGroupCreateMemberUiModel> = emptyList(),

    // 검색 화면에서 아직 확인하지 않은 임시 선택 목록
    val pendingMembers:
    List<AdminStudyGroupCreateMemberUiModel> = emptyList(),

    // 현재 검색어
    val query: String = "",

    // 검색 화면 표시 여부
    val isSearching: Boolean = false,

    // API 로딩 여부
    val isLoading: Boolean = false,

    // 검색 API 결과
    val searchResults:
    List<AdminStudyGroupCreateMemberUiModel> = emptyList(),

    // 다음 페이지 조회 cursor
    val nextCursor: Long? = null,

    // 다음 페이지 존재 여부
    val hasNext: Boolean = true,
) : UiState {

    /**
     * 검색 결과에서 새롭게 선택한 멤버가 있을 때만
     * 확인 버튼을 활성화합니다.
     */
    val isConfirmEnabled: Boolean
        get() = pendingMembers.isNotEmpty()
}

sealed interface GroupCreateMemberPickerEvent : UiEvent