package com.umc.presentation.ui.home.dialog

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.home.LocationItem
import com.umc.domain.model.home.ParticipantItem
import com.umc.domain.usecase.challenger.SearchChallengerScheduleUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class BottomSheetParticipantViewModel @Inject constructor(
    private val searchChallengerScheduleUseCase: SearchChallengerScheduleUseCase
) : BaseViewModel<BottomSheetParticipantUiState, BottomSheetParticipantEvent>(
    BottomSheetParticipantUiState()
) {

    init {
        searchParticipants("")
    }

    // 유저 검색 로직
    fun searchParticipants(query: String) {
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
        fetchParticipants(isNextPage = false)
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
                    size = 20,
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
    fun loadMore() {
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
    fun clearSearch() {
        updateState {
            copy(
                searchResults = emptyList(), // 혹은 초기 리스트(allChallengers)
                searchQuery = "",
                isSearching = false
            )
        }
    }

    fun setSearchingMode(isSearching: Boolean) {
        updateState { copy(isSearching = isSearching) }
    }



    /**csv 로직은 현재 depricated**/
    // CSV에서 추출한 이름들을 실제 객체로 매칭
    fun addFromCsvNames(names: List<String>) {

    }


    //csv에서 가지고온 파일들로 참여자를 세우는 함수
    fun updateParticipant(participants: List<ParticipantItem>){
        updateState {

            val newList = participants.toList()

            // 결과 텍스트 만들기
            val summaryText = when {
                newList.isEmpty() -> ""
                newList.size == 1 -> newList[0].name
                else -> "${newList[0].name} 외 ${newList.size - 1}명"
            }


            copy(
                selectedParticipants = newList,
                selectedParticipantsString = summaryText
            )
        }
    }
}



data class BottomSheetParticipantUiState(
    //선택한 인원들
    val selectedParticipants: List<ParticipantItem> = emptyList(),
    //선택한 인원들 string (...외 2명)
    val selectedParticipantsString: String = "",
    //검색 결과들
    val searchResults: List<ParticipantItem> = emptyList(),
    //다이얼로그에서 찾는 쿼리
    val searchQuery: String = "",
    //찾는 중인가? bottom UI 제어 용도
    val isSearching: Boolean = false,

    //페이징 관련 필드
    val nextCursor: Long? = null, //검색 용도로 이전 페이지의 마지막 챌린저 ID (첫 페이지는 null)
    val hasNext: Boolean = true,
    val isLoading: Boolean = false,


) : UiState {
    val isSelectedParticipant: Boolean
        get() = selectedParticipants.isNotEmpty()
}

sealed interface BottomSheetParticipantEvent : UiEvent {

}