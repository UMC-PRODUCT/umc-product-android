package com.umc.presentation.ui.notice

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.ChallengerRecord
import com.umc.domain.model.UserInfo
import com.umc.domain.model.enums.UserChallengerRole
import com.umc.domain.model.notice.NoticeChipState
import com.umc.domain.model.notice.NoticeSummary
import com.umc.domain.model.organization.GisuItem
import com.umc.domain.usecase.appDataStore.GetUserInfoUseCase
import com.umc.domain.usecase.notice.GetNoticeListUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoticeViewModel @Inject constructor(
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val getNoticeListUseCase: GetNoticeListUseCase
) : BaseViewModel<NoticeUiState, NoticeEvent>(
    NoticeUiState(),
) {
    init {
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

        // challengerRecords가 비어있으면 기본 "전체" 칩만 반환
        if (userInfo.challengerRecords.isEmpty()) {
            updateState { copy(canWriteNotice = false) }
            return chipList
        }

        // 공지 작성 권한 확인 (MEMBER가 아닌 경우에만 권한 있음)
        val hasWritePermission = userInfo.roles.any { role ->
            UserChallengerRole.from(role.roleType) != UserChallengerRole.MEMBER
        }
        updateState { copy(canWriteNotice = hasWritePermission) }

        // challengerRecords를 순회하며 각 기수별 칩 생성
        userInfo.challengerRecords.forEach { record ->
            val gisuId = record.gisuId

            if (record.schoolId != 0L && record.schoolName.isNotEmpty()) {
                val schoolChipText = record.schoolName
                if (chipList.none { it.text == schoolChipText }) {
                    chipList.add(
                        NoticeChipState(
                            text = schoolChipText,
                            schoolId = record.schoolId,
                            gisuId = gisuId
                        )
                    )
                }
            }

            if (!record.chapterName.isNullOrEmpty()) {
                val chapterChipText = record.chapterName!!
                if (chipList.none { it.text == chapterChipText }) {
                    chipList.add(
                        NoticeChipState(
                            text = chapterChipText,
                            chapterId = record.chapterId,
                            gisuId = gisuId
                        )
                    )
                }
            }

            if (record.part.isNotEmpty()) {
                val partChipText = record.part
                if (chipList.none { it.text == partChipText }) {
                    chipList.add(
                        NoticeChipState(
                            text = partChipText,
                            part = record.part,
                            gisuId = gisuId
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

    private fun createDropDownListFromChallengerRecords(challengerRecords: List<ChallengerRecord>): List<GisuItem> {
        return challengerRecords.map { record ->
            GisuItem(
                gisuId = record.gisuId.toInt(),
                generation = record.gisu.toInt(),
                isActive = false
            )
        }.distinctBy { it.gisuId } // 중복 기수 제거
    }

    private fun getMyProfile() = viewModelScope.launch {
        getUserInfoUseCase().collect { userInfo ->
            val dropdownList = createDropDownListFromChallengerRecords(userInfo.challengerRecords)
            
            // challengerRecords가 있으면 첫 번째 기수를 기본 선택
            if (dropdownList.isNotEmpty()) {
                val activeGisu = dropdownList.firstOrNull { it.isActive } ?: dropdownList.first()
                val nowTitle = "${activeGisu.generation}기 공지사항"
                updateNowTitle(nowTitle, activeGisu.gisuId.toLong())
            }
            
            updateDropDownList(dropdownList)
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
