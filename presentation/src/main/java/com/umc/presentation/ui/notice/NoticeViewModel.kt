package com.umc.presentation.ui.notice

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.UserInfo
import com.umc.domain.model.notice.NoticeChipState
import com.umc.domain.model.notice.NoticeSummary
import com.umc.domain.model.organization.GisuItem
import com.umc.domain.model.enums.UserChallengerRole
import com.umc.domain.usecase.appDataStore.GetUserInfoUseCase
import com.umc.domain.usecase.notice.GetNoticeListUseCase
import com.umc.domain.usecase.organization.GetGisuListUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoticeViewModel @Inject constructor(
    private val getGisuListUseCase: GetGisuListUseCase,
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val getNoticeListUseCase: GetNoticeListUseCase
) : BaseViewModel<NoticeUiState, NoticeEvent>(
    NoticeUiState(),
) {
    init {
        getDropDownList()
        getMyProfile()
    }

    private fun updateChipList(chipList: List<NoticeChipState>) {
        updateState {
            copy(
                chipList = chipList
            )
        }
    }

    private fun createChipsFromUserInfo(userInfo: UserInfo): List<NoticeChipState> {
        val chipList = mutableListOf<NoticeChipState>()
        chipList.add(NoticeChipState(text = "전체", isClicked = true))

        // 공지 작성 권한 확인 (MEMBER가 아닌 경우에만 권한 있음)
        val hasWritePermission = userInfo.roles.any { role ->
            UserChallengerRole.from(role.roleType) != UserChallengerRole.MEMBER
        }
        updateState { copy(canWriteNotice = hasWritePermission) }

        if (userInfo.schoolId != 0L) {
            chipList.add(
                NoticeChipState(
                    text = userInfo.schoolName,
                    schoolId = userInfo.schoolId
                )
            )
        }

        userInfo.roles.forEach { role ->
            val chapterName = role.chapterName
            if (!chapterName.isNullOrEmpty() && chipList.none { it.text == "$chapterName 지부" }) {
                chipList.add(
                    NoticeChipState(
                        text = "$chapterName 지부",
                        chapterId = role.chapterId
                    )
                )
            }

            val responsiblePart = role.responsiblePart
            if (!responsiblePart.isNullOrEmpty() && chipList.none { it.text == responsiblePart }) {
                chipList.add(
                    NoticeChipState(
                        text = responsiblePart,
                        part = responsiblePart
                    )
                )
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
        getNoticeList(
            chapterId = clickedItem.chapterId,
            schoolId = clickedItem.schoolId,
            part = clickedItem.part
        )
    }

    fun onClickSearch() {
        emitEvent(NoticeEvent.MoveToSearchEvent(uiState.value.selectedGisu))
    }

    fun updateNowTitle(title: String, gisu: Long) {
        updateState {
            copy(nowTitle = title, selectedGisu = gisu)
        }
        getNoticeList(gisuId = gisu)
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
                updateNowTitle(nowTitle, it.gisuList.find { it.isActive }?.gisuId?.toLong() ?: 0)
                updateDropDownList(it.gisuList)
            }
        )
    }

    private fun getMyProfile() = viewModelScope.launch {
        getUserInfoUseCase().collect { userInfo ->
            updateChipList(createChipsFromUserInfo(userInfo))
        }
    }

    fun getNoticeList(
        gisuId: Long = uiState.value.selectedGisu,
        chapterId: Long? = null,
        schoolId: Long? = null,
        part: String? = null,
        isRefresh: Boolean = true
    ) = viewModelScope.launch {
        val state = uiState.value

        if (state.isPageLoading || (!isRefresh && state.isLastPage)) return@launch

        updateState { copy(isPageLoading = true) }

        val pageToFetch = if (isRefresh) 0 else state.currentPage

        resultResponse(
            response = getNoticeListUseCase(
                gisuId = gisuId,
                chapterId = chapterId,
                schoolId = schoolId,
                part = part,
                page = pageToFetch,
                size = 20
            ),
            successCallback = { noticeSearch ->
                updateState {
                    copy(
                        noticeList = if (isRefresh) noticeSearch.content else noticeList + noticeSearch.content,
                        currentPage = pageToFetch + 1,
                        isPageLoading = false,
                        isLastPage = !noticeSearch.hasNext
                    )
                }
            },
            errorCallback = {
                updateState { copy(isPageLoading = false) }
            }
        )
    }

    fun loadNextPage() {
        if (!uiState.value.isPageLoading && !uiState.value.isLastPage) {
            getNoticeList(isRefresh = false)
        }
    }
}

data class NoticeUiState(
    val isShowDropDown: Boolean = false,
    val isShowSubChip: Boolean = false,
    val nowTitle: String = "",
    val selectedGisu: Long = 0,
    val selectedSubChip: String = "파트",
    val dropdownList: List<GisuItem> = emptyList(),
    val chipList: List<NoticeChipState> = emptyList(),
    val noticeList: List<NoticeSummary> = emptyList(),
    val currentPage: Int = 0,
    val isPageLoading: Boolean = false,
    val isLastPage: Boolean = false,
    val canWriteNotice: Boolean = false
) : UiState

sealed interface NoticeEvent : UiEvent {
    object MoveToWriteEvent : NoticeEvent

    data class MoveToSearchEvent(val gisuId: Long) : NoticeEvent
}
