package com.umc.presentation.ui.notice

import com.umc.domain.model.notice.NoticeChipState
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NoticeViewModel
@Inject
constructor() : BaseViewModel<NoticeUiState, NoticeEvent>(
    NoticeUiState(),
) {
    init {
        updateChipList(getDummy())
    }

    private fun updateChipList(chipList: List<NoticeChipState>) {
        updateState {
            copy(
                chipList = chipList
            )
        }
    }

    fun onClickChip(item: NoticeChipState) {
        val isAll = item.text == "전체"
        val turningAllOn = isAll && !item.isClicked

        val newList = uiState.value.chipList.map { chip ->
            when {
                turningAllOn -> {
                    // "전체"를 켜는 순간: 전체만 true, 나머지 false
                    chip.copy(isClicked = chip.text == "전체")
                }
                chip.text == item.text -> {
                    // 그 외: 클릭한 칩만 토글
                    chip.copy(isClicked = !chip.isClicked)
                }
                else -> chip
            }
        }

        updateChipList(newList)
    }

    private fun getDummy(): List<NoticeChipState> {
        return listOf(
            NoticeChipState(
                text = "전체",
                isClicked = true,
            ),
            NoticeChipState(
                text = "중앙운영사무국",
                isClicked = false,
            ),
            NoticeChipState(
                text = "Ain지부",
                isClicked = false,
            ),
            NoticeChipState(
                text = "중앙대학교",
                isClicked = false,
            )
        )
    }
}

data class NoticeUiState(
    val chipList: List<NoticeChipState> = emptyList(),
) : UiState

sealed interface NoticeEvent : UiEvent {
    object MoveToMainEvent : NoticeEvent

    object MoveToLoginEvent : NoticeEvent
}
