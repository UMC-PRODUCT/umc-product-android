package com.umc.presentation.ui.notice

import com.umc.domain.model.enums.NoticeCategory
import com.umc.domain.model.notice.Notice
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
        updateNoticeList(getDummyNotice())
    }

    private fun updateChipList(chipList: List<NoticeChipState>) {
        updateState {
            copy(
                chipList = chipList
            )
        }
    }

    private fun updateNoticeList(noticeList: List<Notice>) {
        updateState {
            copy(
                noticeList = noticeList
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

    private fun getDummyNotice(): List<Notice> {
        return listOf(
            Notice(
                id = 1,
                isMustRead = true,
                category = NoticeCategory.CENTRAL_OFFICE,
                date = "2026.01.24",
                title = "제목이 들어갈 자리",
                content = "내용이 들어갈자리 어쩌구 저쩌구내용이 들어갈자리 어쩌구 저쩌구내용이 들어갈자리 어쩌구 저쩌구내용이 들어갈자리 어쩌구 저쩌구내용이 들어갈자리 어쩌구 저쩌구내용이 들어갈자리 어쩌구 저쩌구내용이 들어갈자리 어쩌구 저쩌구",
                author = "중앙 운영진",
                count = 1000
            ),
            Notice(
                id = 2,
                isMustRead = true,
                category = NoticeCategory.BRANCH,
                date = "2026.01.24",
                title = "제목이 들어갈 자리",
                content = "내용이 들어갈자리 어쩌구 저쩌구내용이 들어갈자리 어쩌구 저쩌구내용이 들어갈자리 어쩌구 저쩌구내용이 들어갈자리 어쩌구 저쩌구내용이 들어갈자리 어쩌구 저쩌구내용이 들어갈자리 어쩌구 저쩌구내용이 들어갈자리 어쩌구 저쩌구",
                author = "중앙 운영진",
                count = 1000
            ),
            Notice(
                id = 3,
                isMustRead = false,
                category = NoticeCategory.SCHOOL,
                date = "2026.01.24",
                title = "제목이 들어갈 자리",
                content = "내용이 들어갈자리 어쩌구 저쩌구내용이 들어갈자리 어쩌구 저쩌구내용이 들어갈자리 어쩌구 저쩌구내용이 들어갈자리 어쩌구 저쩌구내용이 들어갈자리 어쩌구 저쩌구내용이 들어갈자리 어쩌구 저쩌구내용이 들어갈자리 어쩌구 저쩌구",
                author = "중앙 운영진",
                count = 1000
            ),
            Notice(
                id = 5,
                isMustRead = false,
                category = NoticeCategory.CENTRAL_OFFICE,
                date = "2026.01.24",
                title = "제목이 들어갈 자리",
                content = "내용이 들어갈자리 어쩌구 저쩌구내용이 들어갈자리 어쩌구 저쩌구내용이 들어갈자리 어쩌구 저쩌구내용이 들어갈자리 어쩌구 저쩌구내용이 들어갈자리 어쩌구 저쩌구내용이 들어갈자리 어쩌구 저쩌구내용이 들어갈자리 어쩌구 저쩌구",
                author = "중앙 운영진",
                count = 1000
            ),
            Notice(
                id = 1,
                isMustRead = false,
                category = NoticeCategory.PART,
                date = "2026.01.24",
                title = "제목이 들어갈 자리",
                content = "내용이 들어갈자리 어쩌구 저쩌구내용이 들어갈자리 어쩌구 저쩌구내용이 들어갈자리 어쩌구 저쩌구내용이 들어갈자리 어쩌구 저쩌구내용이 들어갈자리 어쩌구 저쩌구내용이 들어갈자리 어쩌구 저쩌구내용이 들어갈자리 어쩌구 저쩌구",
                author = "중앙 운영진",
                count = 1000
            ),
        )
    }
}

data class NoticeUiState(
    val chipList: List<NoticeChipState> = emptyList(),
    val noticeList: List<Notice> = emptyList()
) : UiState

sealed interface NoticeEvent : UiEvent {
    object MoveToMainEvent : NoticeEvent

    object MoveToLoginEvent : NoticeEvent
}
