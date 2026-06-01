package com.umc.presentation.ui.notice

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.ChallengerRecord
import com.umc.domain.model.UserInfo
import com.umc.domain.model.enums.UserChallengerRole
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.notice.NoticeChipState
import com.umc.domain.model.notice.NoticeSummary
import com.umc.domain.model.organization.GisuItem
import com.umc.domain.usecase.appDataStore.GetUserInfoUseCase
import com.umc.domain.usecase.notice.GetNoticeListUseCase
import com.umc.domain.usecase.organization.GetChapterDetailUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.umc.domain.repository.AppDataStoreRepository

private const val STAFF_NOTICE_CHIP_TEXT = "운영진 공지"

@HiltViewModel
class NoticeViewModel @Inject constructor(
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val getNoticeListUseCase: GetNoticeListUseCase,
    private val getChapterDetailUseCase: GetChapterDetailUseCase,
    private val appDataStoreRepository: AppDataStoreRepository
) : BaseViewModel<NoticeUiState, NoticeEvent>(
    NoticeUiState(),
) {
    private var cachedUserInfo: UserInfo? = null

    init {
        getMyProfile()
        collectReadNoticeIds()
    }

    private fun collectReadNoticeIds() = viewModelScope.launch {
        appDataStoreRepository.getReadNoticeIds().collect { readIds ->
            updateState { copy(readNoticeIds = readIds) }
        }
    }

    fun markNoticeAsRead(noticeId: Long) = viewModelScope.launch {
        appDataStoreRepository.addReadNoticeId(noticeId)
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
        val savedSelectedText = uiState.value.selectedChipText
        val selectedGisu = uiState.value.selectedGisu
        val filteredRecords = if (selectedGisu == 0L) {
            userInfo.challengerRecords
        } else {
            userInfo.challengerRecords.filter { it.gisuId == selectedGisu }
        }

        chipList.add(NoticeChipState(text = "전체", isClicked = savedSelectedText == "전체"))

        // 선택된 기수의 challengerRecords가 비어있으면 기본 "전체" 칩만 반환
        if (filteredRecords.isEmpty()) {
            updateState { copy(canWriteNotice = false) }
            return chipList
        }

        // 공지 작성 권한 확인 (MEMBER가 아닌 경우에만 권한 있음)
        val hasWritePermission = userInfo.roles.any { role ->
            UserChallengerRole.from(role.roleType) != UserChallengerRole.MEMBER
        }
        updateState { copy(canWriteNotice = hasWritePermission) }

        // 스태프 권한이 있을 경우 "운영진 공지" 칩을 "전체" 다음에 삽입
        if (hasWritePermission) {
            val staffChipSelected = savedSelectedText == STAFF_NOTICE_CHIP_TEXT
            chipList.add(NoticeChipState(text = STAFF_NOTICE_CHIP_TEXT, isClicked = staffChipSelected, isStaffNoticeChip = true))
        }

        // 선택된 기수의 challengerRecords를 순회하며 칩 생성
        filteredRecords.forEach { record ->
            val gisuId = record.gisuId

            if (record.schoolId != 0L && record.schoolName.isNotEmpty()) {
                val schoolChipText = record.schoolName
                if (chipList.none { it.text == schoolChipText }) {
                    val isSelected = schoolChipText == savedSelectedText
                    chipList.add(
                        NoticeChipState(
                            text = schoolChipText,
                            schoolId = record.schoolId,
                            gisuId = gisuId,
                            isClicked = isSelected
                        )
                    )
                }
            }

            if (!record.chapterName.isNullOrEmpty()) {
                val chapterChipText = record.chapterName!!
                if (chipList.none { it.text == chapterChipText }) {
                    val isSelected = chapterChipText == savedSelectedText
                    chipList.add(
                        NoticeChipState(
                            text = chapterChipText,
                            chapterId = record.chapterId,
                            gisuId = gisuId,
                            isClicked = isSelected
                        )
                    )
                }
            }

            if (record.part.isNotEmpty()) {
                val partChipText = UserPart.from(record.part).label
                if (chipList.none { it.text == partChipText }) {
                    val isSelected = partChipText == savedSelectedText
                    chipList.add(
                        NoticeChipState(
                            text = partChipText,
                            part = record.part,
                            gisuId = gisuId,
                            isClicked = isSelected
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
        updateState { copy(selectedChipText = clickedItem.text) }

        if (clickedItem.isStaffNoticeChip) {
            getNoticeList(noticeTab = computeStaffNoticeTab(), isRefresh = true)
        } else {
            getNoticeList(
                chapterId = clickedItem.chapterId,
                schoolId = clickedItem.schoolId,
                part = clickedItem.part,
                noticeTab = "CHALLENGER",
                isRefresh = true
            )
        }
    }

    fun onClickSearch() {
        emitEvent(NoticeEvent.MoveToSearchEvent(uiState.value.selectedGisu))
    }

    fun updateNowTitle(title: String, gisu: Long) {
        updateState {
            copy(
                nowTitle = title,
                selectedGisu = gisu,
                selectedChipText = "전체"
            )
        }
        cachedUserInfo?.let { userInfo ->
            updateChipList(createChipsFromUserInfo(userInfo))
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
        }
            .distinctBy { it.gisuId } // 중복 기수 제거
            .sortedByDescending { it.generation } // 최신 기수부터 표시
    }

    private fun getMyProfile() = viewModelScope.launch {
        getUserInfoUseCase().collect { userInfo ->
            cachedUserInfo = userInfo
            val dropdownList = createDropDownListFromChallengerRecords(userInfo.challengerRecords)

            if (dropdownList.isNotEmpty()) {
                val nowGisu = dropdownList.first()
                val nowTitle = "${nowGisu.generation}기 공지사항"
                updateNowTitle(nowTitle, nowGisu.gisuId.toLong())
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
        noticeTab: String = "CHALLENGER",
        isRefresh: Boolean = true
    ) = viewModelScope.launch {
        val state = uiState.value

        if (state.isPageLoading || (!isRefresh && state.isLastPage)) return@launch

        updateState { copy(isPageLoading = true, currentNoticeTab = noticeTab) }

        val pageToFetch = if (isRefresh) 0 else state.currentPage

        resultResponse(
            response = getNoticeListUseCase(
                gisuId = gisuId,
                noticeTab = noticeTab,
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
                loadChapterNamesForNotices(noticeSearch.content)
            },
            errorCallback = {
                updateState { copy(isPageLoading = false) }
            }
        )
    }

    private fun loadChapterNamesForNotices(notices: List<NoticeSummary>) = viewModelScope.launch {
        val chapterIds = notices.mapNotNull { it.targetInfo.targetChapterId }.distinct()

        chapterIds.forEach { chapterId ->
            resultResponse(
                response = getChapterDetailUseCase(chapterId.toLong()),
                successCallback = { chapter ->
                    // 해당 chapterId를 가진 notice들의 targetChapterName 업데이트
                    updateState {
                        val updatedList = noticeList.map { notice ->
                            if (notice.targetInfo.targetChapterId == chapterId) {
                                notice.copy(
                                    targetInfo = notice.targetInfo.copy(
                                        targetChapterName = chapter.name
                                    )
                                )
                            } else {
                                notice
                            }
                        }
                        copy(noticeList = updatedList)
                    }
                }
            )
        }
    }

    fun loadNextPage() {
        if (!uiState.value.isPageLoading && !uiState.value.isLastPage) {
            getNoticeList(noticeTab = uiState.value.currentNoticeTab, isRefresh = false)
        }
    }

    private fun computeStaffNoticeTab(): String {
        val roles = cachedUserInfo?.roles?.map { it.roleType } ?: return "CENTRAL_MEMBER"
        return when {
            roles.any { it in listOf("CENTRAL_PRESIDENT", "CENTRAL_VICE_PRESIDENT", "SUPER_ADMIN", "CHAPTER_PRESIDENT", "CENTRAL_OPERATING_TEAM_MEMBER", "CENTRAL_EDUCATION_TEAM_MEMBER") } -> "CENTRAL_MEMBER"
            roles.any { it in listOf("SCHOOL_PRESIDENT", "SCHOOL_VICE_PRESIDENT", "SCHOOL_ETC_ADMIN") } -> "SCHOOL_CORE"
            roles.any { it == "SCHOOL_PART_LEADER" } -> "SCHOOL_PART_LEADER"
            else -> "CENTRAL_MEMBER"
        }
    }
}

data class NoticeUiState(
    val isShowDropDown: Boolean = false,
    val nowTitle: String = "",
    val selectedGisu: Long = 0,
    val selectedChipText: String = "전체",
    val dropdownList: List<GisuItem> = emptyList(),
    val chipList: List<NoticeChipState> = emptyList(),
    val noticeList: List<NoticeSummary> = emptyList(),
    val currentPage: Int = 0,
    val isPageLoading: Boolean = false,
    val isLastPage: Boolean = false,
    val canWriteNotice: Boolean = false,
    val readNoticeIds: Set<Long> = emptySet(),
    val currentNoticeTab: String = "CHALLENGER"
) : UiState

sealed interface NoticeEvent : UiEvent {
    object MoveToWriteEvent : NoticeEvent

    data class MoveToSearchEvent(val gisuId: Long) : NoticeEvent
}
