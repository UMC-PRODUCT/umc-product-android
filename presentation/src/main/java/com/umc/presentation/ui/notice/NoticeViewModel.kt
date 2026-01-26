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
        updateDropDownList(dummyDropDown())
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
        val newList = uiState.value.chipList.map { chip ->
            chip.copy(isClicked = (chip.text == item.text))
        }

        //TODO 하드코딩임 서버 내려오면 수정
        if (item.text == "중앙운영사무국") {
            updateState { copy(isShowSubChip = true) }
        } else {
            updateState { copy(isShowSubChip = false) }
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

    fun onClickSearch() {
        emitEvent(NoticeEvent.MoveToSearchEvent)
    }

    fun updateNowTitle(title: String) {
        onClickShowDropDown()
        updateState {
            copy(nowTitle = title)
        }
    }

    fun updateDropDownList(list: List<String>) {
        updateState {
            copy(dropdownList = list)
        }
    }

    private fun dummyDropDown() : List<String>{
        return listOf(
            "12기 공지사항", "11기 공지사항", "10기 공지사항", "9기 공지사항", "8기 공지사항",
            "7기 공지사항", "6기 공지사항", "5기 공지사항", "4기 공지사항", "3기 공지사항", "2기 공지사항",
            "1기 공지사항"
        )
    }

    fun onClickShowDropDown() {
        updateState {
            copy(isShowDropDown = !uiState.value.isShowDropDown)
        }
    }

    fun updateSubChip(filter: String) {
        updateState {
            copy(selectedSubChip = filter)
        }
    }

    fun onClickWriteNotice() {
        emitEvent(NoticeEvent.MoveToWriteEvent)
    }
}

data class NoticeUiState(
    val isShowDropDown: Boolean = false,
    val isShowSubChip: Boolean = false,
    val nowTitle: String = "12기 공지사항", //TODO 서버에서 기수만 내려주는지, 텍스트 내려주는지 확인 필요 (아마 전자 같기도)
    val selectedSubChip: String = "파트",
    val dropdownList: List<String> = emptyList(),
    val chipList: List<NoticeChipState> = emptyList(),
    val noticeList: List<Notice> = emptyList()
) : UiState

sealed interface NoticeEvent : UiEvent {
    object MoveToWriteEvent : NoticeEvent

    object MoveToSearchEvent : NoticeEvent
}
