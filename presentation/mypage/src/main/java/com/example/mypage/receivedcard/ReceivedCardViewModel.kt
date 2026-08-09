package com.example.mypage.receivedcard

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.UserInfo
import com.umc.domain.model.mypage.UserCard
import com.umc.domain.model.mypage.UserCardPartType
import com.umc.domain.usecase.appDataStore.usercard.GetUserCardUseCase
import com.umc.domain.usecase.member.GetMyProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ReceivedCardViewModel @Inject constructor(
    private val getMyProfileUseCase: GetMyProfileUseCase, //내 프로필 정보 가져오기
    private val getUserCardUseCase: GetUserCardUseCase, //유저 명함 가져오기

) : BaseViewModel<ReceivedCardUiState, ReceivedCardEvent>(
    ReceivedCardUiState()) {



    //초기 상태
    init {
        loadReceivedCards()
    }


    private fun loadReceivedCards() {

        viewModelScope.launch {
            getUserCardUseCase().collect { cards ->
                updateState {
                    copy(
                        allCards = cards,
                        filteredCards = cards
                    )
                }
            }
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
                            card.part.contains(query, ignoreCase = true) ||
                            card.university.contains(query, ignoreCase = true)
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