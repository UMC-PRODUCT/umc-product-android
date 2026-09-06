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
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
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
            updateState { copy(readNoticeIds = readIds.toImmutableSet()) }
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
                    dropdownList = dropdownList.toImmutableList(),
                    chipList = createFilterChips(userInfo).toImmutableList(),
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
     * 필터 칩 생성. 전체 / 중앙운영사무국 / 지부 / 학교 / 파트를 한 줄에 나열한다.
     *
     * 칩 하나가 서버 파라미터 하나(chapterId·schoolId·part·noticeTab)에 대응하고
     * 항상 하나만 선택된다. 서버가 이 값들을 평면적으로 받기 때문에
     * 여러 칩을 겹쳐 적용하면 조건이 AND로 누적돼 결과가 비어버린다
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

        // 중앙운영사무국 공지(noticeTab=CENTRAL_MEMBER)는 중앙운영진만 조회할 수 있다.
        // 자기 role보다 상위 탭을 요청하면 403이므로 권한이 있을 때만 칩을 노출한다
        if (hasCentralStaffRole(userInfo)) {
            chipList.add(
                NoticeChipState(
                    text = AppStrings.NOTICE_CENTRAL_CHIP,
                    isStaffNoticeChip = true,
                    isClicked = selectedText == AppStrings.NOTICE_CENTRAL_CHIP,
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

    /** 중앙운영사무국 공지(CENTRAL_MEMBER 탭)를 조회할 수 있는 권한인지 */
    private fun hasCentralStaffRole(userInfo: UserInfo): Boolean =
        userInfo.roles.any { UserChallengerRole.from(it.roleType) in CENTRAL_STAFF_ROLES }

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
                }.toImmutableList(),
                selectedChipText = clickedItem.text,
                selectedPart = null,
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
                }.toImmutableList(),
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
            updateState { copy(chipList = createFilterChips(userInfo).toImmutableList()) }
        }
        refreshNoticeList()
    }

    fun onClickShowDropDown() {
        updateState { copy(isShowDropDown = !uiState.value.isShowDropDown) }
    }

    fun onClickSearch() {
        val state = uiState.value
        if (state.selectedGisu <= 0L) return

        // 검색도 목록과 같은 조건으로 조회해야 한다
        val query = buildNoticeQuery(state, computeStaffNoticeTab())
        emitEvent(
            NoticeEvent.MoveToSearchEvent(
                gisuId = state.selectedGisu,
                noticeTab = query.noticeTab,
                chapterId = query.chapterId,
                schoolId = query.schoolId,
                part = query.part,
            )
        )
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

    /**
     * 현재 선택된 필터 조합으로 목록 새로고침.
     * 칩을 바꾼 직후엔 이전 요청이 진행 중이어도 새 조건이 우선이므로 가드를 건너뛴다
     */
    private fun refreshNoticeList() {
        getNoticeList(isRefresh = true, isUserRefresh = true)
    }

    /**
     * 당겨서 새로고침 / 공지 작성·수정 후 목록 갱신.
     * 진행 중인 조회가 있어도 최신 목록이 우선이므로 가드를 무시하고 다시 요청한다
     */
    fun onRefresh() {
        updateState { copy(isRefreshing = true) }
        getNoticeList(isRefresh = true, isUserRefresh = true)
    }

    fun loadNextPage() {
        if (!uiState.value.isPageLoading && !uiState.value.isLastPage) {
            getNoticeList(isRefresh = false)
        }
    }

    private fun getNoticeList(isRefresh: Boolean, isUserRefresh: Boolean = false) = viewModelScope.launch {
        val state = uiState.value

        if (!isUserRefresh && (state.isPageLoading || (!isRefresh && state.isLastPage))) return@launch

        val query = buildNoticeQuery(state, computeStaffNoticeTab())

        // 새로고침이면 페이징 상태도 함께 되돌린다.
        // 이전 필터가 마지막 페이지였는데 그대로 두면 다음 페이지를 영영 못 부른다
        updateState {
            copy(
                isPageLoading = true,
                currentNoticeTab = query.noticeTab,
                isLastPage = if (isRefresh) false else isLastPage,
                errorMessage = null,
            )
        }

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
                        noticeList = if (isRefresh) {
                            noticeSearch.content.toImmutableList()
                        } else {
                            (noticeList + noticeSearch.content).toImmutableList()
                        },
                        currentPage = pageToFetch + 1,
                        isPageLoading = false,
                        isRefreshing = false,
                        isLastPage = !noticeSearch.hasNext,
                    )
                }
            },
            errorCallback = { fail ->
                // 실패를 삼키면 "0건"과 구분되지 않아 원인 파악이 불가능하다
                updateState {
                    copy(isPageLoading = false, isRefreshing = false, errorMessage = fail.message)
                }
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
    val selected = state.chipList.firstOrNull { it.isClicked && it.text != AppStrings.ALL }
        ?: return NoticeQuery(noticeTab = NOTICE_TAB_CHALLENGER)

    // 중앙운영사무국 공지: 탭만 바꾼다.
    // 명세상 chapterId는 쓸 수 없고, schoolId는 넣는 순간 교내 운영진 공지로 분류되므로
    // 중앙운영사무국 공지를 보려면 schoolId를 비워야 한다
    if (selected.isStaffNoticeChip) return NoticeQuery(noticeTab = staffNoticeTab)

    // 그 외는 일반 공지. 칩이 가진 축 하나만 실린다
    return NoticeQuery(
        noticeTab = NOTICE_TAB_CHALLENGER,
        chapterId = selected.chapterId,
        schoolId = selected.schoolId,
        part = selected.part,
    )
}

data class NoticeUiState(
    val isShowDropDown: Boolean = false,
    val nowTitle: String = "",
    val selectedGisu: Long = 0,
    val dropdownList: ImmutableList<GisuItem> = persistentListOf(),
    val chipList: ImmutableList<NoticeChipState> = persistentListOf(),
    val selectedChipText: String = AppStrings.ALL,
    val selectedPart: UserPart? = null,
    val noticeList: ImmutableList<NoticeSummary> = persistentListOf(),
    val currentPage: Int = 0,
    val isPageLoading: Boolean = false,
    /** 당겨서 새로고침 인디케이터 표시 여부 */
    val isRefreshing: Boolean = false,
    /** 조회 실패 사유. null이면 정상(결과가 0건일 수 있음) */
    val errorMessage: String? = null,
    val isLastPage: Boolean = false,
    val canWriteNotice: Boolean = false,
    val readNoticeIds: ImmutableSet<Long> = persistentSetOf(),
    val currentNoticeTab: String = NOTICE_TAB_CHALLENGER,
) : UiState

sealed interface NoticeEvent : UiEvent {

    data class MoveToSearchEvent(
        val gisuId: Long,
        val noticeTab: String,
        val chapterId: Long?,
        val schoolId: Long?,
        val part: String?,
    ) : NoticeEvent

    data class MoveToAdminNoticeEvent(val gisuId: Long) : NoticeEvent

    object MoveToWriteEvent : NoticeEvent

    data class MoveToDetailEvent(val noticeId: Long) : NoticeEvent
}
