package com.umc.presentation.notice

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.component.theme.AppStrings
import com.umc.domain.model.UserInfo
import com.umc.domain.model.enums.UserChallengerRole
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.notice.NoticeChipState
import com.umc.domain.model.notice.NoticeSummary
import com.umc.domain.model.organization.GisuItem
import com.umc.domain.repository.AppDataStoreRepository
import com.umc.domain.usecase.appDataStore.GetUserInfoUseCase
import com.umc.domain.usecase.notice.GetNoticeListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

// 공지 목록 조회 탭 (서버 스펙)
private const val NOTICE_TAB_CHALLENGER = "CHALLENGER"
private const val NOTICE_TAB_CENTRAL_MEMBER = "CENTRAL_MEMBER"
private const val NOTICE_TAB_SCHOOL_CORE = "SCHOOL_CORE"
private const val NOTICE_TAB_SCHOOL_PART_LEADER = "SCHOOL_PART_LEADER"

// 운영진 공지 탭 계산용 role 그룹
private val CENTRAL_STAFF_ROLES = setOf(
    UserChallengerRole.SUPER_ADMIN,
    UserChallengerRole.CENTRAL_PRESIDENT,
    UserChallengerRole.CENTRAL_VICE_PRESIDENT,
    UserChallengerRole.CENTRAL_OPERATING_TEAM_MEMBER,
    UserChallengerRole.CENTRAL_EDUCATION_TEAM_MEMBER,
    UserChallengerRole.CHAPTER_PRESIDENT,
)

private val SCHOOL_CORE_ROLES = setOf(
    UserChallengerRole.SCHOOL_PRESIDENT,
    UserChallengerRole.SCHOOL_VICE_PRESIDENT,
    UserChallengerRole.SCHOOL_ETC_ADMIN,
)

@HiltViewModel
class NoticeViewModel @Inject constructor(
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val getNoticeListUseCase: GetNoticeListUseCase,
    private val appDataStoreRepository: AppDataStoreRepository,
) : BaseViewModel<NoticeUiState, NoticeEvent>(
    NoticeUiState(),
) {
    private var cachedUserInfo: UserInfo? = null

    init {
        getMyProfile()
        collectReadNoticeIds()
    }

    /** 읽은 공지 ID를 dataStore에서 구독해 상태에 반영 (읽지 않은 공지 빨간 점 표시용) */
    private fun collectReadNoticeIds() = viewModelScope.launch {
        appDataStoreRepository.getReadNoticeIds().collect { readIds ->
            updateState { copy(readNoticeIds = readIds) }
        }
    }

    /** 내 정보로 기수 드롭다운·1차 필터 칩·공지 작성 권한을 구성하고 첫 목록을 조회 */
    private fun getMyProfile() = viewModelScope.launch {
        getUserInfoUseCase().collect { userInfo ->
            cachedUserInfo = userInfo
            val dropdownList = createDropDownList(userInfo)

            if (dropdownList.isNotEmpty()) {
                val nowGisu = dropdownList.first()
                updateState {
                    copy(
                        nowTitle = nowGisu.displayText,
                        selectedGisu = nowGisu.gisuId.toLong(),
                    )
                }
            }

            updateWritePermission(userInfo)
            updateState {
                copy(
                    dropdownList = dropdownList,
                    orgChipList = createOrgChips(userInfo),
                )
            }
            refreshNoticeList()
        }
    }

    /** challengerRecords에서 중복 제거된 기수 목록 생성 (최신 기수부터) */
    private fun createDropDownList(userInfo: UserInfo): List<GisuItem> {
        return userInfo.challengerRecords.map { record ->
            GisuItem(
                gisuId = record.gisuId.toInt(),
                generation = record.gisu.toInt(),
                isActive = false
            )
        }
            .distinctBy { it.gisuId }
            .sortedByDescending { it.generation }
    }

    /** 선택된 기수의 소속(지부/학교) 정보로 1차 필터 칩 생성 */
    private fun createOrgChips(userInfo: UserInfo): List<NoticeChipState> {
        val selectedGisu = uiState.value.selectedGisu
        val selectedText = uiState.value.selectedOrgChipText

        val filteredRecords = if (selectedGisu == 0L) {
            userInfo.challengerRecords
        } else {
            userInfo.challengerRecords.filter { it.gisuId == selectedGisu }
        }

        val chipList = mutableListOf(
            NoticeChipState(text = AppStrings.ALL, isClicked = selectedText == AppStrings.ALL)
        )

        filteredRecords.forEach { record ->
            val chapterName = record.chapterName
            if (!chapterName.isNullOrEmpty() && chipList.none { it.text == chapterName }) {
                chipList.add(
                    NoticeChipState(
                        text = chapterName,
                        chapterId = record.chapterId,
                        gisuId = record.gisuId,
                        isClicked = chapterName == selectedText,
                    )
                )
            }

            if (record.schoolId != 0L && record.schoolName.isNotEmpty()
                && chipList.none { it.text == record.schoolName }
            ) {
                chipList.add(
                    NoticeChipState(
                        text = record.schoolName,
                        schoolId = record.schoolId,
                        gisuId = record.gisuId,
                        isClicked = record.schoolName == selectedText,
                    )
                )
            }
        }

        return chipList
    }

    /** 공지 작성 권한 확인 (MEMBER가 아닌 경우에만 권한 있음) */
    private fun updateWritePermission(userInfo: UserInfo) {
        val hasWritePermission = userInfo.roles.any { role ->
            UserChallengerRole.from(role.roleType) != UserChallengerRole.MEMBER
        }
        updateState { copy(canWriteNotice = hasWritePermission) }
    }

    /** 1차 필터(소속) 칩 클릭. 선택 시 2차 필터는 전체로 초기화 */
    fun onClickOrgChip(clickedItem: NoticeChipState) {
        updateState {
            copy(
                orgChipList = orgChipList.map { chip ->
                    chip.copy(isClicked = chip.text == clickedItem.text)
                },
                selectedOrgChipText = clickedItem.text,
                selectedSubChip = NoticeSubChip.ALL,
                selectedPart = null,
            )
        }
        refreshNoticeList()
    }

    /** 2차 필터 - 전체 */
    fun onClickSubChipAll() {
        updateState { copy(selectedSubChip = NoticeSubChip.ALL, selectedPart = null) }
        refreshNoticeList()
    }

    /** 2차 필터 - 운영진 공지 */
    fun onClickSubChipStaff() {
        updateState { copy(selectedSubChip = NoticeSubChip.STAFF, selectedPart = null) }
        refreshNoticeList()
    }

    /** 2차 필터 - 파트 바텀시트에서 파트 선택 */
    fun onSelectPart(part: UserPart) {
        updateState { copy(selectedSubChip = NoticeSubChip.PART, selectedPart = part) }
        refreshNoticeList()
    }

    /** 기수 드롭다운에서 기수 선택. 필터 초기화 후 목록 새로고침 */
    fun onClickGisu(item: GisuItem) {
        updateState {
            copy(
                nowTitle = item.displayText,
                selectedGisu = item.gisuId.toLong(),
                selectedOrgChipText = AppStrings.ALL,
                selectedSubChip = NoticeSubChip.ALL,
                selectedPart = null,
                isShowDropDown = false,
            )
        }
        cachedUserInfo?.let { userInfo ->
            updateState { copy(orgChipList = createOrgChips(userInfo)) }
        }
        refreshNoticeList()
    }

    fun onClickShowDropDown() {
        updateState { copy(isShowDropDown = !uiState.value.isShowDropDown) }
    }

    fun onClickSearch() {
        emitEvent(NoticeEvent.MoveToSearchEvent(uiState.value.selectedGisu))
    }

    fun onClickAdminNotice() {
        emitEvent(NoticeEvent.MoveToAdminNoticeEvent(uiState.value.selectedGisu))
    }

    fun onClickWriteNotice() {
        emitEvent(NoticeEvent.MoveToWriteEvent)
    }

    /** 공지 클릭 시 읽음 처리 후 상세로 이동 */
    fun onClickNotice(noticeId: Long) {
        viewModelScope.launch {
            appDataStoreRepository.addReadNoticeId(noticeId)
        }
        emitEvent(NoticeEvent.MoveToDetailEvent(noticeId))
    }

    /** 현재 선택된 필터 조합으로 목록 새로고침 */
    private fun refreshNoticeList() {
        getNoticeList(isRefresh = true)
    }

    fun loadNextPage() {
        if (!uiState.value.isPageLoading && !uiState.value.isLastPage) {
            getNoticeList(isRefresh = false)
        }
    }

    private fun getNoticeList(isRefresh: Boolean) = viewModelScope.launch {
        val state = uiState.value

        if (state.isPageLoading || (!isRefresh && state.isLastPage)) return@launch

        val selectedOrg = state.orgChipList.firstOrNull {
            it.isClicked && it.text != AppStrings.ALL
        }
        val isStaffNotice = state.selectedSubChip == NoticeSubChip.STAFF
        // 일반 공지는 항상 CHALLENGER 고정, 운영진 공지만 role에 맞는 탭으로 조회 (서버 스펙)
        val noticeTab = if (isStaffNotice) computeStaffNoticeTab() else NOTICE_TAB_CHALLENGER

        updateState { copy(isPageLoading = true, currentNoticeTab = noticeTab) }

        val pageToFetch = if (isRefresh) 0 else state.currentPage

        resultResponse(
            response = getNoticeListUseCase(
                gisuId = state.selectedGisu,
                noticeTab = noticeTab,
                // 운영진 공지는 schoolId 입력 여부로 중앙/교내 공지가 구분되므로 chapterId는 전달하지 않음
                chapterId = if (isStaffNotice) null else selectedOrg?.chapterId,
                schoolId = selectedOrg?.schoolId,
                part = state.selectedPart?.name,
                page = pageToFetch,
                size = 20
            ),
            successCallback = { noticeSearch ->
                updateState {
                    copy(
                        noticeList = if (isRefresh) noticeSearch.content else noticeList + noticeSearch.content,
                        currentPage = pageToFetch + 1,
                        isPageLoading = false,
                        isLastPage = !noticeSearch.hasNext,
                    )
                }
            },
            errorCallback = {
                updateState { copy(isPageLoading = false) }
            }
        )
    }

    /** 운영진 공지 조회 시 사용자 role에 맞는 탭 계산 (서버 스펙) */
    private fun computeStaffNoticeTab(): String {
        val roles = cachedUserInfo?.roles
            ?.map { UserChallengerRole.from(it.roleType) }
            ?: return NOTICE_TAB_CENTRAL_MEMBER

        return when {
            roles.any { it in CENTRAL_STAFF_ROLES } -> NOTICE_TAB_CENTRAL_MEMBER
            roles.any { it in SCHOOL_CORE_ROLES } -> NOTICE_TAB_SCHOOL_CORE
            roles.any { it == UserChallengerRole.SCHOOL_PART_LEADER } -> NOTICE_TAB_SCHOOL_PART_LEADER
            else -> NOTICE_TAB_CENTRAL_MEMBER
        }
    }
}

/** 2차 필터 종류 (1차 필터에서 전체 외 소속 선택 시 노출) */
enum class NoticeSubChip { ALL, STAFF, PART }

data class NoticeUiState(
    val isShowDropDown: Boolean = false,
    val nowTitle: String = "",
    val selectedGisu: Long = 0,
    val dropdownList: List<GisuItem> = emptyList(),
    val orgChipList: List<NoticeChipState> = emptyList(),
    val selectedOrgChipText: String = AppStrings.ALL,
    val selectedSubChip: NoticeSubChip = NoticeSubChip.ALL,
    val selectedPart: UserPart? = null,
    val noticeList: List<NoticeSummary> = emptyList(),
    val currentPage: Int = 0,
    val isPageLoading: Boolean = false,
    val isLastPage: Boolean = false,
    val canWriteNotice: Boolean = false,
    val readNoticeIds: Set<Long> = emptySet(),
    val currentNoticeTab: String = NOTICE_TAB_CHALLENGER,
) : UiState {
    // 1차 필터에서 전체 외 항목 선택 시 2차 필터 노출
    val isSubChipVisible: Boolean
        get() = selectedOrgChipText != AppStrings.ALL
}

sealed interface NoticeEvent : UiEvent {

    data class MoveToSearchEvent(val gisuId: Long) : NoticeEvent

    data class MoveToAdminNoticeEvent(val gisuId: Long) : NoticeEvent

    object MoveToWriteEvent : NoticeEvent

    data class MoveToDetailEvent(val noticeId: Long) : NoticeEvent
}
