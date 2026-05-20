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

    // 부모 뷰가 열릴 때 기존 기등록 상태를 강제 주입 동기화하기 위한 함수
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

    // 유저 검색 로직
    fun searchParticipants(query: String) {
        //이전 작업 취소
        searchJob?.cancel()

        //쿼리가 비어있으면 검색X
        if (query.isBlank()) {
            clearParticipantSearch()
            return
        }

        //일단 현재 상태를 반영해서
        updateState {
            copy(
                searchQuery = query,
                searchResults = emptyList(),
                nextCursor = null,
                hasNext = true,
                isSearching = query.isNotBlank()
            )
        }
        searchJob = viewModelScope.launch {
            delay(500) //'박ㅇ' 등이 완성되어 '박유수'가 될 때까지 기다림
            fetchParticipants(isNextPage = false)
        }
    }


    // 실질적으로 usecae로 유저 데이터를 가져오는 로직
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
                    Log.d("log_home", "유저검색 성공: ${response.content}")
                    updateState {
                        copy(
                            searchResults = response.content,
                            nextCursor = response.nextCursor,
                            hasNext = response.hasNext,
                            isLoading = false
                        )
                    }
                },
                errorCallback = {
                    //검색 실패 시, 로딩 해제 및 다음 꺼 X
                    Log.d("log_home", "유저검색 실패: ${it.message}")
                    updateState { copy(isLoading = false, hasNext = false) }
                }
            )
        }
    }

    // 무한 스크롤 로직 (바닥 도달 시 추가 데이터 로드)
    fun loadMoreParticipants() {
        val state = uiState.value
        // 로딩 중이거나 다음 페이지가 없으면 중단
        if (state.isLoading || !state.hasNext) return

        fetchParticipants(isNextPage = true)
    }

    // 인원 토글 로직
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



    //검색 기록을 초기화하하고 중지하는 함수
    fun clearParticipantSearch() {
        searchJob?.cancel()
        updateState {
            copy(
                searchResults = emptyList(), // 혹은 초기 리스트(allChallengers)
                searchQuery = "",
                isSearching = false
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
) : UiState {
    val isSelectedParticipant: Boolean get() = selectedParticipants.isNotEmpty()
}

sealed interface ScheduleChallengerAddDialogEvent : UiEvent