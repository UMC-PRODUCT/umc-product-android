package com.umc.presentation.ui.home.dialog

import android.util.Log
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.home.LocationItem
import com.umc.domain.model.home.ParticipantItem
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class BottomSheetParticipantViewModel @Inject constructor(

) : BaseViewModel<BottomSheetParticipantUiState, BottomSheetParticipantEvent>(
    BottomSheetParticipantUiState()
) {

    // 임시 더미 데이터 (실제로는 서버나 DB에서 가져와야 함)
    private val allChallengers = listOf(
        ParticipantItem("박유수", UserPart.ANDROID, "숭실대학교"),
        ParticipantItem("어헛차", UserPart.ANDROID, "숭실대학교"),
        ParticipantItem("김하나", UserPart.DESIGN, "서울대학교"),
        ParticipantItem("이두리", UserPart.IOS, "고려대학교"),
        ParticipantItem("최삼이", UserPart.NODE_JS, "연세대학교"),
        ParticipantItem("박사성", UserPart.ANDROID, "숭실대학교")
    )

    // 유저 검색 로직
    fun searchParticipants(query: String) {
        val results = if (query.isBlank()) {
            allChallengers
        } else {
            allChallengers.filter { it.name.contains(query, ignoreCase = true) }
        }
        Log.d("log_home", "query: $query, isSearching: ${query.isNotBlank()}")
        updateState { copy(searchResults = results, searchQuery = query, isSearching = query.isNotBlank()) }
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



    // CSV에서 추출한 이름들을 실제 객체로 매칭
    fun addFromCsvNames(names: List<String>) {
        val matchedUsers = names.mapNotNull { name ->
            allChallengers.find { it.name == name }
        }
        updateState {
            val combined = (selectedParticipants + matchedUsers).distinctBy { it.id }
            copy(selectedParticipants = combined)
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
    val isSearching: Boolean = false

) : UiState {
    val isSelectedParticipant: Boolean
        get() = selectedParticipants.isNotEmpty()
}

sealed interface BottomSheetParticipantEvent : UiEvent {

}