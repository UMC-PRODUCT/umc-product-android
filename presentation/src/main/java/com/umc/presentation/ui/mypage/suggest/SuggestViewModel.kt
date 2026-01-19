package com.umc.presentation.ui.mypage.suggest

import com.umc.domain.model.enums.SuggestionStatus
import com.umc.domain.model.mypage.SuggestionItem
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.String


@HiltViewModel
class SuggestViewModel @Inject
constructor() : BaseViewModel<SuggestFragmentUiState, SuggestFragmentEvent>(
    SuggestFragmentUiState()){

    fun navigateSuggestWrite() {
        emitEvent(SuggestFragmentEvent.NavigateSuggestWrite)
    }

}



data class SuggestFragmentUiState(
    val dummy: String = "",
    val tmpData: List<SuggestionItem> = listOf(
        SuggestionItem(
            status =  SuggestionStatus.COMPLETED,
            date = "2026.01.19",
            title = "세션 시간 조정을 건의합니다.",
            content = "7시는 너무 늦소! 6시 30분으로 조정 가능할까요?",
            adminAnswer = "네 운영진 회의 후 적극 반영하겠습니다."
        ),
        SuggestionItem(
            status =  SuggestionStatus.WAITING,
            date = "2026.01.19",
            title = "추워요",
            content = "추워요.",
            adminAnswer = null
        ),
        SuggestionItem(
            status =  SuggestionStatus.COMPLETED,
            date = "2026.01.19",
            title = "제목을 길게 해볼게요오오오오오오오오오오오오오오오오오오오오오오오오오오오오오오오오오",
            content = "내용도 길게 해볼게요오오오오오오오오오오오오오오오오오오오오오오오오오오오오오오오오오오오오오오오오오오오오오오",
            adminAnswer = "답변도 길게요오오오오오오오오오오오오오오오오오오오오오오오오오오오오오오오오오오오오오오오오오오오오오오오오오"
        ),
        SuggestionItem(
            status =  SuggestionStatus.WAITING,
            date = "2026.01.19",
            title = "세션 시간 조정을 건의합니다.",
            content = "7시는 너무 일러요! 7시 30분으로 조정 가능할까요?",
            adminAnswer = null
        )
    )
) : UiState

sealed interface SuggestFragmentEvent : UiEvent {
    object NavigateSuggestWrite : SuggestFragmentEvent
}
