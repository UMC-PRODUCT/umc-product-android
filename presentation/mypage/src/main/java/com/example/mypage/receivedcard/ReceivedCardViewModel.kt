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
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
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
        //명함 호출
        loadReceivedCards()
    }


    /**
     * AppDataStore로부터 보관된 유저 명함 리스트를 실시간으로 수신받아 UI State에 바인딩하는 메서드
     */
    private fun loadReceivedCards() {

        viewModelScope.launch {
            getUserCardUseCase().collect { cards ->
                updateState {
                    copy(
                        allCards = cards.toImmutableList(),
                        filteredCards = cards.toImmutableList()
                    )
                }
            }
        }
    }

    /**
     * 사용자가 입력한 검색어 키워드를 기반으로 명함 데이터 목록을 실시간 필터링하는 메서드
     *
     * @param query 검색창에 입력된 텍스트
     */
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
                }.toImmutableList()
            }
            copy(searchQuery = query, filteredCards = filtered)
        }
    }

    /**
     * 검색어를 초기화하고 필터링 리스트를 전체 명함 목록으로 되돌리는 메서드
     */
    fun clearSearchQuery() {
        onSearchQueryChanged("")
    }



}

data class ReceivedCardUiState(

    val searchQuery: String = "",
    val allCards: ImmutableList<UserCard> = persistentListOf(), //전체 명함
    val filteredCards: ImmutableList<UserCard> = persistentListOf(), //검색 명함
    val isLoading: Boolean = false

    ) : UiState

sealed interface ReceivedCardEvent : UiEvent {
    //이동하기
    object NavigateToBack: ReceivedCardEvent


}