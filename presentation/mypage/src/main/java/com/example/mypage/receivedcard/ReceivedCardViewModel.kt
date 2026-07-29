package com.example.mypage.receivedcard

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.UserInfo
import com.umc.domain.model.mypage.UserCard
import com.umc.domain.model.mypage.UserCardPartType
import com.umc.domain.usecase.member.GetMyProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ReceivedCardViewModel @Inject constructor(
    private val getMyProfileUseCase: GetMyProfileUseCase, //내 프로필 정보 가져오기

) : BaseViewModel<ReceivedCardUiState, ReceivedCardEvent>(
    ReceivedCardUiState()) {



    //초기 상태
    init {
        viewModelScope.launch {
            loadReceivedCards()
        }
    }


    private fun loadReceivedCards() {
        // 더미 데이터 초기화 (실제로는 Repository 연결)
        val mockCards = listOf(
            UserCard("1", "박유수", "어헛차", "00대학교", UserCardPartType.ADMIN),
            UserCard("2", "김도연", "도리", "00대학교", UserCardPartType.PM),
            UserCard("3", "양지애", "나루", "00대학교", UserCardPartType.DESIGN),
            UserCard("4", "조경석", "조나단", "00대학교", UserCardPartType.WEB),
            UserCard("5", "홍길동", "안드로이드", "00대학교", UserCardPartType.ANDROID),
            UserCard("6", "김스프링", "서버", "00대학교", UserCardPartType.SPRING),
            UserCard("7", "이노드", "백엔드", "00대학교", UserCardPartType.NODEJS),
            UserCard("8", "박아이폰", "앱개발", "00대학교", UserCardPartType.IOS)
        )

        updateState {
            copy(
                allCards = mockCards,
                filteredCards = mockCards
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        updateState {
            val filtered = if (query.isBlank()) {
                allCards
            } else {
                allCards.filter { card ->
                    card.name.contains(query, ignoreCase = true) ||
                            card.nickname.contains(query, ignoreCase = true) ||
                            card.part.label.contains(query, ignoreCase = true) ||
                            card.schoolName.contains(query, ignoreCase = true)
                }
            }
            copy(searchQuery = query, filteredCards = filtered)
        }
    }

    fun clearSearchQuery() {
        onSearchQueryChanged("")
    }



}

data class ReceivedCardUiState(

    val searchQuery: String = "",
    val allCards: List<UserCard> = emptyList(), //전체 명함
    val filteredCards: List<UserCard> = emptyList(), //검색 명함
    val isLoading: Boolean = false

    ) : UiState

sealed interface ReceivedCardEvent : UiEvent {
    //이동하기
    object NavigateToBack: ReceivedCardEvent


}