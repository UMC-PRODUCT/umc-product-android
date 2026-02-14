package com.umc.presentation.ui.notice.detail

import com.umc.domain.model.enums.NoticeCategory
import com.umc.domain.model.enums.VoteCondition
import com.umc.domain.model.enums.VoteState
import com.umc.domain.model.notice.NoticeDetail
import com.umc.domain.model.notice.User
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
    NoticeFragmentUiState()
) {

    init {

    }

    fun onClickBackPressed() {
        emitEvent(NoticeFragmentEvent.MoveBackPressedEvent)
    }

    //TODO 투표 클릭
//    fun onClickVoteItem(clicked: VoteItem) {
//        updateState {
//            val detail = uiState.value.detail
//            val vote = detail.vote
//
//            val isSingle = VoteCondition.SINGLE_VOTE in vote.condition
//            val isMultiple = VoteCondition.MULTIPLE_VOTE in vote.condition
//
//            val newItems = when {
//                isSingle -> {
//                    vote.item.map { item ->
//                        item.copy(isChecked = item.name == clicked.name)
//                    }
//
//                    val clickedWasChecked =
//                        vote.item.any { it.name == clicked.name && it.isChecked }
//                    vote.item.map { item ->
//                        when {
//                            item.name == clicked.name -> item.copy(isChecked = !clickedWasChecked)
//                            else -> item.copy(isChecked = false)
//                        }
//                    }
//                }
//
//                isMultiple -> {
//                    vote.item.map { item ->
//                        if (item.name == clicked.name) item.copy(isChecked = !item.isChecked) else item
//                    }
//                }
//
//                else -> {
//                    vote.item.map { item ->
//                        if (item.name == clicked.name) item.copy(isChecked = !item.isChecked) else item
//                    }
//                }
//            }
//
//            copy(
//                detail = detail.copy(
//                    vote = vote.copy(
//                        item = newItems
//                    )
//                )
//            )
//        }
//    }

    fun onClickShowBottomSheet() {
        emitEvent(NoticeFragmentEvent.ShowBottomSheetEvent)
    }


}


data class NoticeFragmentUiState(
    val detail: NoticeDetail = NoticeDetail()
) : UiState

sealed interface NoticeFragmentEvent : UiEvent {
    object ShowBottomSheetEvent : NoticeFragmentEvent
    object MoveBackPressedEvent : NoticeFragmentEvent
}