package com.umc.presentation.ui.notice

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.UserInfo
import com.umc.domain.model.enums.NoticeCategory
import com.umc.domain.model.notice.Notice
import com.umc.domain.model.notice.NoticeChipState
import com.umc.domain.model.organization.GisuItem
import com.umc.domain.usecase.appDataStore.GetUserInfoUseCase
import com.umc.domain.usecase.member.GetMyProfileUseCase
import com.umc.domain.usecase.organization.GetGisuListUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import com.umc.presentation.util.ULog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoticeViewModel @Inject constructor(
    private val getGisuListUseCase: GetGisuListUseCase,
    private val getUserInfoUseCase: GetUserInfoUseCase
) : BaseViewModel<NoticeUiState, NoticeEvent>(
    NoticeUiState(),
) {
    init {
        getDropDownList()
        getMyProfile()
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

    private fun createChipsFromUserInfo(userInfo: UserInfo): List<NoticeChipState> {
        val chipList = mutableListOf<NoticeChipState>()
        chipList.add(NoticeChipState(text = "전체", isClicked = true))

        if (userInfo.schoolId != 0L) {
            chipList.add(
                NoticeChipState(
                    text = userInfo.schoolName,
                    schoolId = userInfo.schoolId
                )
            )
        }

        userInfo.roles.forEach { role ->
            if (role.organizationType == "CENTRAL") {
                chipList.add(NoticeChipState(text = "중앙운영사무국", chapterId = null)) // 중앙은 별도 ID 처리 혹은 null
            } else {
                chipList.add(NoticeChipState(text = "${role.organizationType} 지부", chapterId = role.organizationId))
            }

            if (role.responsiblePart.isNotEmpty()) {
                if (chipList.none { it.text == role.responsiblePart }) {
                    chipList.add(
                        NoticeChipState(
                            text = role.responsiblePart,
                            part = role.responsiblePart
                        )
                    )
                }
            }
        }

        return chipList
    }

    fun onClickChip(clickedItem: NoticeChipState) {
        val newList = uiState.value.chipList.map { chip ->
            chip.copy(isClicked = (chip.text == clickedItem.text))
        }
        updateChipList(newList)

        if (clickedItem.text == "중앙운영사무국") {
            updateState { copy(isShowSubChip = true) }
        } else {
            updateState { copy(isShowSubChip = false) }
        }
//        fetchNoticeList(
//            gisuId = currentGisuId,
//            chapterId = clickedItem.chapterId,
//            schoolId = clickedItem.schoolId,
//            part = clickedItem.part
//        )
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
        updateState {
            copy(nowTitle = title)
        }
    }

    fun updateDropDownList(list: List<GisuItem>) {
        updateState {
            copy(dropdownList = list)
        }
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

    private fun getDropDownList() = viewModelScope.launch {
        resultResponse(
            response = getGisuListUseCase(),
            successCallback = {
                val nowTitle = "${it.gisuList.find { it.isActive }?.generation}기 공지사항"
                updateNowTitle(nowTitle)
                updateDropDownList(it.gisuList)
            }
        )
    }

    private fun getMyProfile() = viewModelScope.launch {
        getUserInfoUseCase().collect { userInfo ->
            updateChipList(createChipsFromUserInfo(userInfo))
        }
    }
}

data class NoticeUiState(
    val isShowDropDown: Boolean = false,
    val isShowSubChip: Boolean = false,
    val nowTitle: String = "",
    val selectedSubChip: String = "파트",
    val dropdownList: List<GisuItem> = emptyList(),
    val chipList: List<NoticeChipState> = emptyList(),
    val noticeList: List<Notice> = emptyList()
) : UiState

sealed interface NoticeEvent : UiEvent {
    object MoveToWriteEvent : NoticeEvent

    object MoveToSearchEvent : NoticeEvent
}
