package com.umc.presentation.home.schedule.dialog
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.home.ParticipantItem
import com.umc.domain.usecase.challenger.SearchChallengerScheduleUseCase

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScheduleChallengerAddDialogViewModel @Inject constructor(
    private val searchChallengerScheduleUseCase: SearchChallengerScheduleUseCase
) : BaseViewModel<ScheduleChallengerAddDialogUiState, ScheduleChallengerAddDialogEvent>(
    ScheduleChallengerAddDialogUiState()
) {

    // 검색 작업 스케줄링 제어용 Job 레퍼런스
    private var searchJob: Job? = null

    /**
     * 상위 스크린에서 선택된 기존 챌린저 목록을 수신하여 다이얼로그 내부 UI State로 동기화하는 메서드
     * 일정 수정 시 기존 참여 챌린저를 dialog에도 반영하기 위함
     *
     * @param list 현재 선택되어 있는 ParticipantItem 리스트
     */
    fun setSelectedParticipant(list : List<ParticipantItem>) {
        updateState {
            val summaryText = when {
                list.isEmpty() -> ""
                list.size == 1 -> list[0].name
                else -> "${list[0].name} 외 ${list.size - 1}명"
            }
            copy(
                selectedParticipants = list,
                selectedParticipantsString = summaryText
            )
        }
    }

    /**
     * 검색 쿼리를 입력받아 500ms 디바운스 처리 후 서버 조회를 요청하는 메서드
     *
     * @param query 사용자가 입력한 검색 문자열
     */
    fun searchParticipants(query: String) {
        // 이전 검색 대기 작업이 존재하는 경우 즉시 취소
        searchJob?.cancel()

        // 검색어가 비어있는 경우 결과를 초기화하고 검색 취소
        if (query.isBlank()) {
            clearParticipantSearch()
            return
        }

        // 일단 현재 상태를 반영해서
        updateState {
            copy(
                searchQuery = query,
                searchResults = emptyList(),
                nextCursor = null,
                hasNext = true,
                isSearching = query.isNotBlank(),
                // 디바운스 대기 중에는 '결과 없음'/'실패' 문구가 깜빡이지 않도록 초기화
                hasSearched = false,
                isSearchFailed = false
            )
        }
        searchJob = viewModelScope.launch {
            delay(500) //'박ㅇ' 등이 완성되어 '박유수'가 될 때까지 기다림
            fetchParticipants(isNextPage = false)
        }
    }


    /**
     * UseCase를 호출하여 챌린저 목록 데이터를 서버에서 페이징 수신하는 메서드
     *
     * @param isNextPage 첫 진입 검색인지, 추가 페이지 수신인지 구분하는 플래그
     */
    private fun fetchParticipants(isNextPage: Boolean) {
        val state = uiState.value

        //API 호출중임을 표시
        updateState {
            copy(
                isLoading = true,
            ) }

        viewModelScope.launch {
            // UseCase 호출: 다음 페이지면 보관된 커서 사용, 아니면 null(처음)
            val cursor = if (isNextPage) state.nextCursor else null

            resultResponse(
                response = searchChallengerScheduleUseCase(
                    cursor = cursor,
                    size = 50,
                    name = state.searchQuery.ifBlank { null } // 빈 검색어는 null로 그 외는 searchParticipant에서 가져온 쿼리로
                ),
                successCallback = { response ->
                    // 요청 이후 검색어가 바뀌었으면 늦게 도착한 이전 응답이므로 버린다
                    if (uiState.value.searchQuery != state.searchQuery) return@resultResponse
                    Log.d("log_home", "유저검색 성공: ${response.content}")
                    updateState {
                        copy(
                            searchResults = response.content,
                            nextCursor = response.nextCursor,
                            hasNext = response.hasNext,
                            isLoading = false,
                            hasSearched = true,
                            isSearchFailed = false
                        )
                    }
                },
                errorCallback = {
                    //검색 실패 시, 로딩 해제 및 다음 꺼 X
                    if (uiState.value.searchQuery != state.searchQuery) return@resultResponse
                    Log.d("log_home", "유저검색 실패: ${it.message}")
                    updateState { copy(isLoading = false, hasNext = false, hasSearched = true, isSearchFailed = true) }
                }
            )
        }
    }

    /**
     * 스크롤이 하단에 도달했을 때 추가 챌린저 목록을 페이징 조회하는 메서드
     */
    fun loadMoreParticipants() {
        val state = uiState.value
        // 로딩 중이거나 다음 페이지가 없으면 중단
        if (state.isLoading || !state.hasNext) return

        fetchParticipants(isNextPage = true)
    }

    /**
     * 특정 챌린저를 선택 목록에 추가하거나 제거하고, 상단 표시용 요약 문구를 갱신하는 메서드
     *
     * @param user 토글할 챌린저 객체
     */
    fun toggleParticipant(user: ParticipantItem) {
        updateState {
            val isExist = selectedParticipants.any { it.id == user.id }
            val newList = if (isExist) {
                selectedParticipants.filter { it.id != user.id }
            } else {
                selectedParticipants + user
            }

            //결과 스트링 작성
            val summaryText = when {
                newList.isEmpty() -> ""
                newList.size == 1 -> newList[0].name
                else -> "${newList[0].name} 외 ${newList.size - 1}명"
            }

            copy(selectedParticipants = newList,
                selectedParticipantsString = summaryText
            )
        }
    }


    /**
     * 진행 중인 검색 코루틴을 취소하고 검색 결과 목록과 검색어를 초기화하는 메서드
     */
    fun clearParticipantSearch() {
        searchJob?.cancel()
        updateState {
            copy(
                searchResults = emptyList(), // 혹은 초기 리스트(allChallengers)
                searchQuery = "",
                isSearching = false,
                // 버려지는 진행 중 요청이 로딩 상태를 풀어 주지 못하므로 여기서 해제한다
                isLoading = false,
                hasSearched = false,
                isSearchFailed = false
            )
        }
    }
}

data class ScheduleChallengerAddDialogUiState(
    val selectedParticipants: List<ParticipantItem> = emptyList(),
    val selectedParticipantsString: String = "",
    val searchResults: List<ParticipantItem> = emptyList(),
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val nextCursor: Long? = null,
    val hasNext: Boolean = true,
    val isLoading: Boolean = false,
    // 검색 응답(성공/실패)을 한 번이라도 받았는지. 디바운스 대기 중과 '결과 없음'을 구분한다.
    val hasSearched: Boolean = false,
    // 마지막 검색 요청이 실패했는지
    val isSearchFailed: Boolean = false,
) : UiState {
    val isSelectedParticipant: Boolean get() = selectedParticipants.isNotEmpty()
}

sealed interface ScheduleChallengerAddDialogEvent : UiEvent