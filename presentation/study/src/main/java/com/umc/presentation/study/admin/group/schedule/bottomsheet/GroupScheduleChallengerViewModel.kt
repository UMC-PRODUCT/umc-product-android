package com.umc.presentation.study.admin.group.schedule.bottomsheet

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.usecase.challenger.SearchChallengerScheduleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 스터디 일정에 초대할 챌린저 선택 상태를 관리하는 ViewModel
 *
 * 주요 기능
 * - 기존 선택 챌린저 초기화
 * - 이름 기반 챌린저 검색
 * - 검색 결과 커서 페이지네이션
 * - 챌린저 선택 및 선택 해제
 * - 선택 챌린저 요약 문구 생성
 * - 사용자 프로필 이미지 정보 매핑
 */
@HiltViewModel
class GroupScheduleChallengerViewModel @Inject constructor(
    private val searchChallengerScheduleUseCase:
    SearchChallengerScheduleUseCase,
) : BaseViewModel<
        GroupScheduleChallengerState,
        GroupScheduleChallengerEvent,
        >(
    GroupScheduleChallengerState()
) {

    /**
     * 검색 debounce 및 이전 검색 요청 취소를 위한 Job
     */
    private var searchJob: Job? = null

    /**
     * 바텀시트를 열 때 기존에 선택되어 있던 챌린저를 설정합니다.
     *
     * 기존 선택 목록을 그대로 유지하고,
     * 선택된 인원에 맞는 요약 문구를 생성합니다.
     */
    fun setSelected(
        list: List<GroupScheduleChallengerUiModel>,
    ) {
        updateState {
            copy(
                selectedChallengers = list.toImmutableList(),
                selectedSummaryText = makeSummaryText(list),
                hasConfirmButton = list.isNotEmpty(),
            )
        }
    }

    /**
     * 챌린저 검색어를 입력합니다.
     *
     * 검색어가 비어 있으면 검색 상태만 초기화하며,
     * 검색어가 존재하면 300ms debounce 후 API를 호출합니다.
     */
    fun searchChallengers(
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
                hasNext = true,
                hasConfirmButton = true,
            )
        }

        searchJob = viewModelScope.launch {
            delay(300)

            fetchChallengers(
                isNextPage = false
            )
        }
    }

    /**
     * 챌린저를 선택하거나 선택 해제합니다.
     *
     * 이미 선택된 챌린저를 누르면 제거하고,
     * 선택되지 않은 챌린저를 누르면 목록에 추가합니다.
     */
    fun toggleChallenger(
        item: GroupScheduleChallengerUiModel,
    ) {
        updateState {
            val exists = selectedChallengers.any {
                    challenger ->
                challenger.id == item.id
            }

            val newList = if (exists) {
                selectedChallengers.filterNot {
                        challenger ->
                    challenger.id == item.id
                }.toImmutableList()
            } else {
                (selectedChallengers + item).toImmutableList()
            }

            copy(
                selectedChallengers = newList,
                selectedSummaryText = makeSummaryText(newList),
            )
        }
    }

    /**
     * 검색 상태만 초기화합니다.
     *
     * 이미 선택된 챌린저 목록은 유지됩니다.
     */
    fun clearSearchOnly() {
        searchJob?.cancel()

        updateState {
            copy(
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
     * 검색 화면에서 확인한 뒤 검색 상태를 초기화합니다.
     *
     * 선택된 챌린저 목록은 유지합니다.
     */
    fun resetAfterConfirm() {
        searchJob?.cancel()

        updateState {
            copy(
                query = "",
                isSearching = false,
                isLoading = false,
                searchResults = persistentListOf(),
                nextCursor = null,
                hasNext = true,
                hasConfirmButton =
                    selectedChallengers.isNotEmpty(),
            )
        }
    }

    /**
     * 챌린저 선택 화면의 모든 상태를 초기화합니다.
     *
     * 검색 상태뿐 아니라 선택된 챌린저 목록까지 제거합니다.
     */
    fun resetAll() {
        searchJob?.cancel()

        updateState {
            copy(
                selectedChallengers = persistentListOf(),
                selectedSummaryText = "",
                query = "",
                isSearching = false,
                isLoading = false,
                searchResults = persistentListOf(),
                nextCursor = null,
                hasNext = true,
                hasConfirmButton = false,
            )
        }
    }

    /**
     * 선택된 챌린저 목록을 화면에 표시할 요약 문구로 변환합니다.
     *
     * 예)
     * 0명 -> ""
     * 1명 -> "홍길동"
     * 3명 -> "홍길동 외 2명"
     */
    private fun makeSummaryText(
        list: List<GroupScheduleChallengerUiModel>,
    ): String {
        return when {
            list.isEmpty() -> ""

            list.size == 1 ->
                list[0].name

            else ->
                "${list[0].name} 외 ${list.size - 1}명"
        }
    }

    /**
     * 챌린저 검색 API를 호출합니다.
     *
     * @param isNextPage
     * false이면 새로운 검색,
     * true이면 기존 검색 결과의 다음 페이지를 조회합니다.
     */
    private fun fetchChallengers(
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

                    /**
                     * 검색 API 응답을
                     * 일정 초대 챌린저 UI 모델로 변환합니다.
                     */
                    val mappedResults =
                        response.content.map { participant ->
                            GroupScheduleChallengerUiModel(
                                id = participant.id,
                                name = participant.name,

                                // 이름 / 닉네임 / 기수를 조합
                                displayName = buildString {
                                    append(participant.name)

                                    if (
                                        participant.nickname.isNotBlank()
                                    ) {
                                        append("/")
                                        append(participant.nickname)
                                    }

                                    if (participant.gisu > 0) {
                                        append("(")
                                        append(participant.gisu)
                                        append("기)")
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
                                                    mappedResults
                                            ).distinctBy { challenger ->
                                            challenger.id
                                        }.toImmutableList()
                                } else {
                                    mappedResults.distinctBy {
                                            challenger ->
                                        challenger.id
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
     * 검색 결과의 다음 페이지를 조회합니다.
     */
    fun loadMoreChallengers() {
        val currentState = uiState.value

        if (
            currentState.isLoading ||
            !currentState.hasNext ||
            !currentState.isSearching
        ) {
            return
        }

        fetchChallengers(
            isNextPage = true
        )
    }
}

/**
 * 일정 초대 챌린저 선택 화면의 UI 상태
 */
data class GroupScheduleChallengerState(

    /** 현재 선택된 챌린저 목록 */
    val selectedChallengers:
    ImmutableList<GroupScheduleChallengerUiModel> = persistentListOf(),

    /** 선택된 챌린저를 요약한 화면 표시 문구 */
    val selectedSummaryText: String = "",

    /** 현재 입력된 검색어 */
    val query: String = "",

    /** 검색 화면 여부 */
    val isSearching: Boolean = false,

    /** 검색 API 로딩 여부 */
    val isLoading: Boolean = false,

    /** 확인 버튼 표시 여부 */
    val hasConfirmButton: Boolean = false,

    /** 챌린저 검색 결과 */
    val searchResults:
    ImmutableList<GroupScheduleChallengerUiModel> = persistentListOf(),

    /** 다음 페이지 조회 cursor */
    val nextCursor: Long? = null,

    /** 다음 페이지 존재 여부 */
    val hasNext: Boolean = true,
) : UiState

/**
 * 일정 초대 챌린저 화면에서 사용하는 사용자 UI 모델
 *
 * 프로필 이미지 URL까지 포함하여
 * 검색 결과와 선택 목록에서 실제 사용자 이미지를 표시합니다.
 */
data class GroupScheduleChallengerUiModel(
    val id: Long,
    val name: String,
    val displayName: String,
    val partLabel: String,
    val school: String,

    /** 사용자 프로필 이미지 URL */
    val profileImageUrl: String? = null,
)

/**
 * 일정 초대 챌린저 화면의 일회성 UI 이벤트
 */
sealed interface GroupScheduleChallengerEvent : UiEvent