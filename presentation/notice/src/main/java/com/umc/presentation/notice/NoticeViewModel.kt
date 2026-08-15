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

    /** 2차 필터 - 운영진 공지. 소속 필터와 함께 걸 수 없으므로 1차를 되돌린다 */
    fun onClickSubChipStaff() {
        updateState {
            resetOrgSelection().copy(selectedSubChip = NoticeSubChip.STAFF, selectedPart = null)
        }
        refreshNoticeList()
    }

    /** 2차 필터 - 파트 바텀시트에서 파트 선택. 파트도 소속과 함께 걸 수 없다 */
    fun onSelectPart(part: UserPart) {
        updateState {
            resetOrgSelection().copy(selectedSubChip = NoticeSubChip.PART, selectedPart = part)
        }
        refreshNoticeList()
    }

    /**
     * 1차(소속) 선택을 전체로 되돌린다.
     *
     * 서버에는 소속(chapterId·schoolId)·파트·운영진 탭 중 하나만 보낼 수 있어서,
     * 2차에서 파트나 운영진 공지를 고르면 1차 선택은 어차피 쿼리에서 빠진다.
     * 칩 하이라이트를 남겨두면 "선택했는데 반영되지 않는" 상태가 되므로 함께 해제한다
     */
    private fun NoticeUiState.resetOrgSelection(): NoticeUiState = copy(
        orgChipList = orgChipList.map { it.copy(isClicked = it.text == AppStrings.ALL) },
        selectedOrgChipText = AppStrings.ALL,
    )

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

        val query = buildNoticeQuery(state, computeStaffNoticeTab())

        updateState { copy(isPageLoading = true, currentNoticeTab = query.noticeTab) }

        val pageToFetch = if (isRefresh) 0 else state.currentPage

        resultResponse(
            response = getNoticeListUseCase(
                gisuId = state.selectedGisu,
                noticeTab = query.noticeTab,
                chapterId = query.chapterId,
                schoolId = query.schoolId,
                part = query.part,
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

    /**
     * 현재 필터 상태를 서버 쿼리로 바꾼다.
     *
     * 소속(chapterId·schoolId) / 파트 / 운영진 탭은 **한 번에 하나만** 전달한다.
     * 여럿을 함께 보내면 조건이 AND로 누적돼 결과가 비고, 특히 운영진 공지에서는
     * 명세상 chapterId·part를 쓸 수 없으며 schoolId는 중앙/교내 운영진 공지를
     * 가르는 기준이라 소속 칩 값을 그대로 실어 보내면 의미가 어긋난다
     */
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

/** 공지 목록 조회 쿼리. 소속·파트·운영진 탭 중 하나만 채워진다 */
internal data class NoticeQuery(
    val noticeTab: String,
    val chapterId: Long? = null,
    val schoolId: Long? = null,
    val part: String? = null,
)

/**
 * 현재 필터 상태를 서버 쿼리로 바꾼다.
 *
 * 소속(chapterId·schoolId) / 파트 / 운영진 탭은 **한 번에 하나만** 전달한다.
 * 여럿을 함께 보내면 조건이 AND로 누적돼 결과가 비고, 특히 운영진 공지에서는
 * 명세상 chapterId·part를 쓸 수 없으며 schoolId는 중앙/교내 운영진 공지를
 * 가르는 기준이라 소속 칩 값을 그대로 실어 보내면 의미가 어긋난다
 */
internal fun buildNoticeQuery(state: NoticeUiState, staffNoticeTab: String): NoticeQuery {
    val org = state.orgChipList.firstOrNull { it.isClicked && it.text != AppStrings.ALL }

    return when (state.selectedSubChip) {
        // 운영진 공지: role에 맞는 탭만. 소속·파트는 전달하지 않는다
        NoticeSubChip.STAFF -> NoticeQuery(noticeTab = staffNoticeTab)

        // 파트: part만
        NoticeSubChip.PART -> NoticeQuery(
            noticeTab = NOTICE_TAB_CHALLENGER,
            part = state.selectedPart?.name,
        )

        // 전체: 1차 소속만 (칩 구성상 지부와 학교는 동시에 선택될 수 없다)
        NoticeSubChip.ALL -> NoticeQuery(
            noticeTab = NOTICE_TAB_CHALLENGER,
            chapterId = org?.chapterId,
            schoolId = org?.schoolId,
        )
    }
}

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
    /**
     * 1차에서 전체 외 항목을 골랐을 때 2차 필터 노출.
     * 2차에서 파트·운영진 공지를 고르면 1차가 전체로 돌아가므로,
     * 그 상태에서도 선택을 되돌릴 수 있도록 2차 선택이 남아 있으면 계속 노출한다
     */
    val isSubChipVisible: Boolean
        get() = selectedOrgChipText != AppStrings.ALL || selectedSubChip != NoticeSubChip.ALL
}

sealed interface NoticeEvent : UiEvent {

    data class MoveToSearchEvent(val gisuId: Long) : NoticeEvent

    data class MoveToAdminNoticeEvent(val gisuId: Long) : NoticeEvent

    object MoveToWriteEvent : NoticeEvent

    data class MoveToDetailEvent(val noticeId: Long) : NoticeEvent
}
