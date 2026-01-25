package com.umc.presentation.ui.notice.search.result

import com.umc.domain.model.enums.NoticeCategory
import com.umc.domain.model.notice.Notice
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NoticeSearchResultViewModel
@Inject
constructor() : BaseViewModel<NoticeSearchResultUiState, NoticeSearchResultEvent>(
    NoticeSearchResultUiState(),
) {
    init {
        updateNoticeList(getDummyNotice())
    }

    private fun updateNoticeList(noticeList: List<Notice>) {
        updateState {
            copy(
                noticeList = noticeList
            )
        }
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

data class NoticeSearchResultUiState(
    val noticeList: List<Notice> = emptyList()
) : UiState

sealed interface NoticeSearchResultEvent : UiEvent {

}
