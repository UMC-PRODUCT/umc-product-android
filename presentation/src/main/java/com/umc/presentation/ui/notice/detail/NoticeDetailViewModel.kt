package com.umc.presentation.ui.notice.detail

import com.umc.domain.model.enums.NoticeCategory
import com.umc.domain.model.enums.VoteCondition
import com.umc.domain.model.enums.VoteState
import com.umc.domain.model.notice.NoticeDetail
import com.umc.domain.model.notice.Vote
import com.umc.domain.model.notice.VoteItem
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NoticeDetailViewModel @Inject
constructor() : BaseViewModel<NoticeFragmentUiState, NoticeFragmentEvent>(
    NoticeFragmentUiState()){

    init {
        updateState {
            copy(detail = getDummy())
        }
    }

    fun onClickBackPressed(){
        emitEvent(NoticeFragmentEvent.MoveBackPressedEvent)
    }

    private fun getDummy(): NoticeDetail{
        return NoticeDetail(
            mustRead = true,
            category = NoticeCategory.CENTRAL_OFFICE,
            title = "[필독] 12기 활동 규정 안내",
            profileImage = "",
            author = "운영진",
            date = "2026.01.01",
            viewCount = 100,
            receiver = "12기/전체",
            content = "대충 내용이 들어갈 자리",
            imageList = emptyList(),
            link = "https://example.com/guideline",
            vote = Vote(
                title = "회식 메뉴 투표",
                state = VoteState.PROGRESS,
                condition = listOf(VoteCondition.BLINDNESS, VoteCondition.SINGLE_VOTE),
                conditionText = VoteCondition.buildVoteConditionText(listOf(VoteCondition.BLINDNESS, VoteCondition.SINGLE_VOTE)),
                item = listOf(
                    VoteItem(
                        isChecked = false,
                        name = "삼겹살 목살"
                    ),
                    VoteItem(
                        isChecked = false,
                        name = "치킨 피자"
                    ),
                    VoteItem(
                        isChecked = false,
                        name = "족발 보쌈"
                    )
                )
            ),
            allReceiverCount = 1000,
            nowReceiverCount = 801.toString(),
            receiverText = "/ 1000명 (80%)"
        )
    }

    fun onClickVoteItem(clicked: VoteItem) {
        updateState {
            val detail = uiState.value.detail
            val vote = detail.vote

            val isSingle = VoteCondition.SINGLE_VOTE in vote.condition
            val isMultiple = VoteCondition.MULTIPLE_VOTE in vote.condition

            val newItems = when {
                isSingle -> {
                    vote.item.map { item ->
                        item.copy(isChecked = item.name == clicked.name)
                    }

                     val clickedWasChecked = vote.item.any { it.name == clicked.name && it.isChecked }
                     vote.item.map { item ->
                         when {
                             item.name == clicked.name -> item.copy(isChecked = !clickedWasChecked)
                             else -> item.copy(isChecked = false)
                         }
                     }
                }

                isMultiple -> {
                    vote.item.map { item ->
                        if (item.name == clicked.name) item.copy(isChecked = !item.isChecked) else item
                    }
                }

                else -> {
                    vote.item.map { item ->
                        if (item.name == clicked.name) item.copy(isChecked = !item.isChecked) else item
                    }
                }
            }

            copy(
                detail = detail.copy(
                    vote = vote.copy(
                        item = newItems
                    )
                )
            )
        }
    }


}



data class NoticeFragmentUiState(
    val detail: NoticeDetail = NoticeDetail()
) : UiState

sealed class NoticeFragmentEvent : UiEvent {
    object MoveBackPressedEvent : NoticeFragmentEvent()
}