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

            // 칩 구성이 작성 권한(운영진 공지 칩 노출)에 의존하므로 권한을 먼저 반영한다
            updateWritePermission(userInfo)
            updateState {
                copy(
                    dropdownList = dropdownList,
                    chipList = createFilterChips(userInfo),
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

    /**
     * 필터 칩 생성. 전체 / 운영진 공지 / 지부 / 학교 / 파트를 한 줄에 평면 나열한다.
     *
     * 칩 하나가 서버 파라미터 하나(chapterId·schoolId·part·noticeTab)에 대응하며
     * 서로 배타적으로만 선택된다. 여러 칩을 겹쳐 적용하면 조건이 AND로 누적돼
     * 결과가 비어버리므로 항상 한 칩만 활성 상태여야 한다
     */
    private fun createFilterChips(userInfo: UserInfo): List<NoticeChipState> {
        val selectedGisu = uiState.value.selectedGisu
        val selectedText = uiState.value.selectedChipText

        val filteredRecords = if (selectedGisu == 0L) {
            userInfo.challengerRecords
        } else {
            userInfo.challengerRecords.filter { it.gisuId == selectedGisu }
        }

        val chipList = mutableListOf(
            NoticeChipState(text = AppStrings.ALL, isClicked = selectedText == AppStrings.ALL)
        )

        if (uiState.value.canWriteNotice) {
            chipList.add(
                NoticeChipState(
                    text = AppStrings.NOTICE_STAFF_CHIP,
                    isStaffNoticeChip = true,
                    isClicked = selectedText == AppStrings.NOTICE_STAFF_CHIP,
                )
            )
        }

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

        // 파트 칩은 바텀시트로 파트를 고른 뒤 활성화된다
        val selectedPart = uiState.value.selectedPart
        chipList.add(
            NoticeChipState(
                text = selectedPart?.label ?: AppStrings.PART,
                part = selectedPart?.name,
                hanBottomSheet = true,
                isClicked = selectedPart != null,
            )
        )

        return chipList
    }

    /** 공지 작성 권한 확인 (MEMBER가 아닌 경우에만 권한 있음) */
    private fun updateWritePermission(userInfo: UserInfo) {
        val hasWritePermission = userInfo.roles.any { role ->
            UserChallengerRole.from(role.roleType) != UserChallengerRole.MEMBER
        }
        updateState { copy(canWriteNotice = hasWritePermission) }
    }

    /** 필터 칩 클릭. 다른 칩은 모두 해제해 항상 한 조건으로만 조회한다 */
    fun onClickChip(clickedItem: NoticeChipState) {
        updateState {
            copy(
                chipList = chipList.map { chip ->
                    chip.copy(isClicked = chip.text == clickedItem.text)
                },
                selectedChipText = clickedItem.text,
                // 파트 외 칩을 고르면 파트 선택은 해제된다
                selectedPart = if (clickedItem.hanBottomSheet) selectedPart else null,
            )
        }
        refreshNoticeList()
    }

    /** 파트 바텀시트에서 파트 선택. 파트 칩만 활성화된다 */
    fun onSelectPart(part: UserPart) {
        updateState {
            copy(
                selectedPart = part,
                selectedChipText = part.label,
                chipList = chipList.map { chip ->
                    if (chip.hanBottomSheet) {
                        chip.copy(text = part.label, part = part.name, isClicked = true)
                    } else {
                        chip.copy(isClicked = false)
                    }
                },
            )
        }
        refreshNoticeList()
    }

    /** 기수 드롭다운에서 기수 선택. 필터 초기화 후 목록 새로고침 */
    fun onClickGisu(item: GisuItem) {
        updateState {
            copy(
                nowTitle = item.displayText,
                selectedGisu = item.gisuId.toLong(),
                selectedChipText = AppStrings.ALL,
                selectedPart = null,
                isShowDropDown = false,
            )
        }
        cachedUserInfo?.let { userInfo ->
            updateState { copy(chipList = createFilterChips(userInfo)) }
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

        val selectedChip = state.chipList.firstOrNull {
            it.isClicked && it.text != AppStrings.ALL
        }
        val isStaffNotice = selectedChip?.isStaffNoticeChip == true
        // 일반 공지는 항상 CHALLENGER 고정, 운영진 공지만 role에 맞는 탭으로 조회 (서버 스펙)
        val noticeTab = if (isStaffNotice) computeStaffNoticeTab() else NOTICE_TAB_CHALLENGER

        updateState { copy(isPageLoading = true, currentNoticeTab = noticeTab) }

        val pageToFetch = if (isRefresh) 0 else state.currentPage

        resultResponse(
            response = getNoticeListUseCase(
                gisuId = state.selectedGisu,
                noticeTab = noticeTab,
                // 칩 하나가 파라미터 하나에 대응한다. 선택되지 않은 축은 반드시 null이어야
                // 조건이 AND로 누적되지 않는다.
                // 운영진 공지는 소속·파트 필터를 쓰지 않는다 (명세상 챌린저 공지 전용이고,
                // schoolId는 중앙/교내 운영진 공지를 가르는 기준이라 임의로 넣으면 안 됨)
                chapterId = if (isStaffNotice) null else selectedChip?.chapterId,
                schoolId = if (isStaffNotice) null else selectedChip?.schoolId,
                part = if (isStaffNotice) null else selectedChip?.part,
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

data class NoticeUiState(
    val isShowDropDown: Boolean = false,
    val nowTitle: String = "",
    val selectedGisu: Long = 0,
    val dropdownList: List<GisuItem> = emptyList(),
    val chipList: List<NoticeChipState> = emptyList(),
    val selectedChipText: String = AppStrings.ALL,
    val selectedPart: UserPart? = null,
    val noticeList: List<NoticeSummary> = emptyList(),
    val currentPage: Int = 0,
    val isPageLoading: Boolean = false,
    val isLastPage: Boolean = false,
    val canWriteNotice: Boolean = false,
    val readNoticeIds: Set<Long> = emptySet(),
    val currentNoticeTab: String = NOTICE_TAB_CHALLENGER,
) : UiState

sealed interface NoticeEvent : UiEvent {

    data class MoveToSearchEvent(val gisuId: Long) : NoticeEvent

    data class MoveToAdminNoticeEvent(val gisuId: Long) : NoticeEvent

    object MoveToWriteEvent : NoticeEvent

    data class MoveToDetailEvent(val noticeId: Long) : NoticeEvent
}
