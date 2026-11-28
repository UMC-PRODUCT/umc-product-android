package com.umc.presentation.notice

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.component.theme.AppStrings
import com.umc.domain.model.UserInfo
import com.umc.domain.model.enums.UserChallengerRole
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.notice.noticeRolesForGisu
import com.umc.domain.model.notice.noticeWriterRole
import com.umc.domain.model.notice.NoticeChipState
import com.umc.domain.model.notice.NoticeSummary
import com.umc.domain.model.organization.GisuItem
import com.umc.presentation.notice.adminnotice.AdminNoticeTab
import com.umc.domain.repository.AppDataStoreRepository
import com.umc.domain.usecase.appDataStore.GetUserInfoUseCase
import com.umc.domain.usecase.notice.GetNoticeListUseCase
import com.umc.domain.usecase.organization.GetSearchGisusUseCase
import com.umc.domain.usecase.organization.GetActiveGisuUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

// 공지 목록 조회 탭 (서버 스펙)
private const val NOTICE_TAB_CHALLENGER = "CHALLENGER"
private const val NOTICE_TAB_CENTRAL_MEMBER = "CENTRAL_MEMBER"
private const val NOTICE_TAB_SCHOOL_CORE = "SCHOOL_CORE"
private const val NOTICE_TAB_SCHOOL_PART_LEADER = "SCHOOL_PART_LEADER"

// 운영진 공지 탭 계산용 role 그룹. 지부장은 서버에서 SCHOOL_CORE로 매핑되므로 중앙 칩에서 제외한다.
private val CENTRAL_STAFF_ROLES = setOf(
    UserChallengerRole.CENTRAL_PRESIDENT,
    UserChallengerRole.CENTRAL_VICE_PRESIDENT,
    UserChallengerRole.CENTRAL_OPERATING_TEAM_MEMBER,
    UserChallengerRole.CENTRAL_EDUCATION_TEAM_MEMBER,
)

private val SCHOOL_CORE_ROLES = setOf(
    UserChallengerRole.CHAPTER_PRESIDENT,
    UserChallengerRole.SCHOOL_PRESIDENT,
    UserChallengerRole.SCHOOL_VICE_PRESIDENT,
)

@HiltViewModel
class NoticeViewModel @Inject constructor(
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val getNoticeListUseCase: GetNoticeListUseCase,
    private val getSearchGisusUseCase: GetSearchGisusUseCase,
    private val getActiveGisuUseCase: GetActiveGisuUseCase,
    private val appDataStoreRepository: AppDataStoreRepository,
) : BaseViewModel<NoticeUiState, NoticeEvent>(
    NoticeUiState(),
) {
    private var cachedUserInfo: UserInfo? = null
    private var allGisus: List<GisuItem> = emptyList()
    private var isLoadingAllGisus = false
    private var serverActiveGisuId: Long? = null
    private var isLoadingActiveGisu = false

    // 진행 중인 목록 요청. 필터를 바꾸면 취소해 이전 조건의 응답이 새 목록에 섞이지 않게 한다
    private var listJob: Job? = null

    init {
        getMyProfile()
        loadActiveGisu()
        collectReadNoticeIds()
    }

    /** 작성 권한은 서버 활성 기수로 판단한다. 프로필이 먼저 도착했으면 응답 후 버튼 노출을 다시 계산한다. */
    private fun loadActiveGisu() {
        if (serverActiveGisuId != null || isLoadingActiveGisu) return
        isLoadingActiveGisu = true
        viewModelScope.launch {
            try {
                val response = getActiveGisuUseCase()
                currentCoroutineContext().ensureActive()
                resultResponse(
                    response = response,
                    successCallback = { gisu ->
                        serverActiveGisuId = gisu.gisuId.takeIf { it > 0 }?.toLong()
                        cachedUserInfo?.let(::updateWritePermission)
                    },
                )
            } finally {
                isLoadingActiveGisu = false
            }
        }
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
            if (userInfo.isSuperAdmin && allGisus.isEmpty() && !isLoadingAllGisus) loadAllGisus()
            val dropdownList = createDropDownList(userInfo)
            updateGisuOptions(userInfo, dropdownList)
            refreshNoticeList()
        }
    }

    /** Super Admin은 전체 기수, 그 외에는 챌린저 기록과 역할이 있는 기수를 표시한다. */
    private fun createDropDownList(userInfo: UserInfo): List<GisuItem> {
        val recordGisus = userInfo.challengerRecords.map { record ->
            GisuItem(
                gisuId = record.gisuId.toInt(),
                generation = record.gisu.toInt(),
                isActive = false
            )
        }
        val roleGisus = userInfo.roles.map { role ->
            GisuItem(gisuId = role.gisuId.toInt(), generation = role.gisu.toInt())
        }
        val currentGisu = userInfo.currentGisuMemberInfo?.let { current ->
            GisuItem(gisuId = current.gisuId.toInt(), generation = current.generation.toInt())
        }
        return (if (userInfo.isSuperAdmin) allGisus + recordGisus + roleGisus + listOfNotNull(currentGisu)
                else recordGisus + roleGisus + listOfNotNull(currentGisu))
            .filter { it.gisuId > 0 && it.generation > 0 }
            .distinctBy { it.gisuId }
            .sortedByDescending { it.generation }
    }

    private fun loadAllGisus() {
        if (isLoadingAllGisus) return
        // 프로필 수집과 새로고침이 함께 호출해도 요청은 한 번만 시작한다.
        isLoadingAllGisus = true
        viewModelScope.launch {
            try {
                resultResponse(
                    response = getSearchGisusUseCase(),
                    successCallback = { result ->
                        allGisus = result.gisuList
                        cachedUserInfo?.let { updateGisuOptions(it, createDropDownList(it)) }
                        refreshNoticeList()
                    },
                    errorCallback = { fail -> updateState { copy(errorMessage = fail.message) } },
                )
            } finally {
                isLoadingAllGisus = false
            }
        }
    }

    private fun updateGisuOptions(userInfo: UserInfo, options: List<GisuItem>) {
        updateState { resolveNoticeGisuOptions(this, userInfo, options) }
        val chips = createFilterChips(userInfo)
        if (chips.none { it.isClicked }) updateState { copy(selectedChipText = AppStrings.COMMON_ALL) }
        updateState { copy(chipList = createFilterChips(userInfo).toImmutableList()) }
        updateWritePermission(userInfo)
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
            NoticeChipState(text = AppStrings.COMMON_ALL, isClicked = selectedText == AppStrings.COMMON_ALL)
        )

        // 중앙운영사무국 공지(noticeTab=CENTRAL_MEMBER) 탭은 중앙운영진과 시스템 Super Admin에게만 허용된다.
        // 자기 role보다 상위 탭을 요청하면 403이므로 권한이 있을 때만 칩을 노출한다
        if (userInfo.isSuperAdmin || userInfo.noticeRolesForGisu(selectedGisu).any { it in CENTRAL_STAFF_ROLES }) {
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
                text = selectedPart?.label ?: AppStrings.COMMON_PART,
                part = selectedPart?.name,
                hanBottomSheet = true,
                isClicked = selectedPart != null,
            )
        )

        return chipList
    }

    /** 공지 작성 권한 확인. 작성은 선택한 과거 기수가 아니라 서버가 권한을 판정하는 현재 기수 역할을 사용한다. */
    private fun updateWritePermission(userInfo: UserInfo) {
        val activeGisuId = serverActiveGisuId ?: userInfo.currentGisuMemberInfo?.gisuId ?: 0L
        val hasWritePermission = userInfo.noticeWriterRole(activeGisuId) != null
        updateState { copy(canWriteNotice = hasWritePermission) }
    }

    /** 필터 칩 클릭. 다른 칩은 모두 해제해 항상 한 조건으로만 조회한다 */
    fun onClickChip(clickedItem: NoticeChipState) {
        updateState {
            copy(
                chipList = chipList.map { chip ->
                    // 파트 칩은 다른 칩을 누르면 선택했던 파트 문구를 지우고 초기 상태('파트')로 되돌린다
                    if (chip.hanBottomSheet) {
                        chip.copy(text = AppStrings.COMMON_PART, part = null, isClicked = false)
                    } else {
                        chip.copy(isClicked = chip.text == clickedItem.text)
                    }
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
        if (item.gisuId.toLong() == uiState.value.selectedGisu) {
            updateState { copy(isShowDropDown = false) }
            return
        }
        updateState {
            copy(
                nowTitle = item.displayText,
                selectedGisu = item.gisuId.toLong(),
                canOpenAdminNotice = cachedUserInfo?.canOpenAdminNotice(item.gisuId.toLong()) == true,
                selectedChipText = AppStrings.COMMON_ALL,
                selectedPart = null,
                isShowDropDown = false,
                noticeList = persistentListOf(),
                currentPage = 0,
                isLastPage = false,
                errorMessage = null,
                isNextPageError = false,
                isPageLoading = false,
                isRefreshing = false,
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
        if (!uiState.value.canOpenAdminNotice) return
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
        if (uiState.value.selectedGisu <= 0L) {
            listJob?.cancel()
            updateState { clearNoticeList(this) }
            return
        }
        getNoticeList(isRefresh = true, isUserRefresh = true)
    }

    /**
     * 당겨서 새로고침 / 공지 작성·수정 후 목록 갱신.
     * 진행 중인 조회가 있어도 최신 목록이 우선이므로 가드를 무시하고 다시 요청한다
     */
    fun onRefresh() {
        loadActiveGisu()
        updateState { copy(isRefreshing = true) }
        if (uiState.value.selectedGisu <= 0L) {
            listJob?.cancel()
            updateState { clearNoticeList(this) }
            if (cachedUserInfo?.isSuperAdmin == true) loadAllGisus()
            return
        }
        getNoticeList(isRefresh = true, isUserRefresh = true)
        // 프로필 폴백으로 기수가 선택되어 있어도 전체 기수 목록 조회의 실패는 복구한다.
        if (cachedUserInfo?.isSuperAdmin == true && allGisus.isEmpty()) loadAllGisus()
    }

    fun loadNextPage() {
        if (!uiState.value.isPageLoading && !uiState.value.isLastPage) {
            getNoticeList(isRefresh = false)
        }
    }

    private fun getNoticeList(isRefresh: Boolean, isUserRefresh: Boolean = false) {
        val state = uiState.value

        // 가드를 launch 밖에서 확인한다. 걸린 호출이 listJob을 덮어쓰면 진행 중인 요청을 더는 취소할 수 없다
        if (state.selectedGisu <= 0L) return
        if (!isUserRefresh && (listJob?.isActive == true || state.isPageLoading || (!isRefresh && state.isLastPage))) return

        // 늦게 끝난 이전 필터의 요청(특히 다음 페이지)이 새 목록에 결과나 실패 표시를 남기지 않게 먼저 취소한다
        listJob?.cancel()
        listJob = fetchNoticeList(state, isRefresh)
    }

    private fun fetchNoticeList(state: NoticeUiState, isRefresh: Boolean) = viewModelScope.launch {
        val query = buildNoticeQuery(state, computeStaffNoticeTab())

        // 이전 목록이 마지막 페이지였어도 새 조회 결과에 따라 다음 페이지를 다시 요청할 수 있게 한다.
        updateState {
            copy(
                isPageLoading = true,
                currentNoticeTab = query.noticeTab,
                isLastPage = if (isRefresh) false else isLastPage,
                errorMessage = null,
                isNextPageError = false,
            )
        }

        val pageLoad = loadUniqueNoticePage(
            existing = if (isRefresh) emptyList() else state.noticeList,
            firstPage = if (isRefresh) 0 else state.currentPage,
        ) { page ->
            getNoticeListUseCase(
                gisuId = state.selectedGisu,
                noticeTab = query.noticeTab,
                chapterId = query.chapterId,
                schoolId = query.schoolId,
                part = query.part,
                page = page,
                size = 20,
            )
        }
        currentCoroutineContext().ensureActive()
        if (uiState.value.selectedGisu != state.selectedGisu ||
            buildNoticeQuery(uiState.value, computeStaffNoticeTab()) != query) return@launch
        resultResponse(
            response = pageLoad.response,
            successCallback = { noticeSearch ->
                updateState {
                    copy(
                        noticeList = noticeSearch.content.toImmutableList(),
                        currentPage = pageLoad.pageNumber + 1,
                        isPageLoading = false,
                        isRefreshing = false,
                        isLastPage = !noticeSearch.hasNext,
                        isNextPageError = false,
                    )
                }
            },
            errorCallback = { fail ->
                // 실패를 삼키면 "0건"과 구분되지 않아 원인 파악이 불가능하다
                // 다음 페이지 실패는 목록 끝에 다시 시도 버튼을 띄워 당겨서 새로고침 없이도 다시 부를 수 있게 한다
                val isNextPageError = !isRefresh || pageLoad.pageNumber > 0
                updateState {
                    copy(
                        // 새 조회의 첫 페이지가 비어 있었다면 이전 목록을 새 조회 결과와 섞지 않는다.
                        noticeList = if (isRefresh && isNextPageError) persistentListOf() else noticeList,
                        currentPage = if (isNextPageError) pageLoad.pageNumber else currentPage,
                        isPageLoading = false,
                        isRefreshing = false,
                        errorMessage = fail.message,
                        isNextPageError = isNextPageError,
                    )
                }
            }
        )
    }

    /** 운영진 공지 조회 시 선택 기수의 역할로 탭을 계산한다. */
    private fun computeStaffNoticeTab(): String {
        val userInfo = cachedUserInfo ?: return NOTICE_TAB_CHALLENGER
        val roles = userInfo.noticeRolesForGisu(uiState.value.selectedGisu)

        return when {
            userInfo.isSuperAdmin || roles.any { it in CENTRAL_STAFF_ROLES } -> NOTICE_TAB_CENTRAL_MEMBER
            roles.any { it in SCHOOL_CORE_ROLES } -> NOTICE_TAB_SCHOOL_CORE
            roles.any { it == UserChallengerRole.SCHOOL_PART_LEADER || it == UserChallengerRole.SCHOOL_ETC_ADMIN } -> NOTICE_TAB_SCHOOL_PART_LEADER
            else -> NOTICE_TAB_CHALLENGER
        }
    }
}

internal fun clearNoticeList(state: NoticeUiState): NoticeUiState = state.copy(
    noticeList = persistentListOf(), currentPage = 0, isLastPage = false,
    isPageLoading = false, isRefreshing = false, errorMessage = null, isNextPageError = false,
)

internal fun resolveNoticeGisuOptions(
    state: NoticeUiState,
    userInfo: UserInfo,
    options: List<GisuItem>,
): NoticeUiState {
    val selected = options.firstOrNull { it.gisuId.toLong() == state.selectedGisu }
        ?: options.firstOrNull { it.isActive }
        ?: options.firstOrNull()
    val changedGisu = state.selectedGisu != selected?.gisuId?.toLong()
    return state.copy(
        selectedGisu = selected?.gisuId?.toLong() ?: 0L,
        nowTitle = selected?.displayText.orEmpty(),
        dropdownList = options.toImmutableList(),
        canOpenAdminNotice = userInfo.canOpenAdminNotice(selected?.gisuId?.toLong() ?: 0L),
        selectedChipText = if (changedGisu) AppStrings.COMMON_ALL else state.selectedChipText,
        // 자동 기수 변경도 수동 선택과 동일하게 이전 기수의 파트 조건을 해제한다.
        selectedPart = if (changedGisu) null else state.selectedPart,
        noticeList = if (changedGisu) persistentListOf() else state.noticeList,
        currentPage = if (changedGisu) 0 else state.currentPage,
        isLastPage = if (changedGisu) false else state.isLastPage,
        errorMessage = if (changedGisu) null else state.errorMessage,
        isNextPageError = if (changedGisu) false else state.isNextPageError,
        isPageLoading = if (changedGisu) false else state.isPageLoading,
        isRefreshing = if (changedGisu) false else state.isRefreshing,
    )
}

/** 운영진 공지 화면과 같은 선택 기수의 역할을 기준으로 진입 여부를 판단한다. */
internal fun UserInfo.canOpenAdminNotice(gisuId: Long): Boolean =
    gisuId > 0 && (isSuperAdmin || AdminNoticeTab.highestAccessibleTab(noticeRolesForGisu(gisuId)) != null)

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
 * 여럿을 함께 보내면 조건이 AND로 누적돼 결과가 비고, 운영진 공지에서
 * schoolId를 지정하면 중앙 발신 공지가 제외된다. schoolId를 비우면 서버가 조회자 역할에 따라
 * 허용된 중앙 발신·교내 공지 범위를 적용한다.
 */
internal fun buildNoticeQuery(state: NoticeUiState, staffNoticeTab: String): NoticeQuery {
    val selected = state.chipList.firstOrNull { it.isClicked && it.text != AppStrings.COMMON_ALL }
        ?: return NoticeQuery(noticeTab = NOTICE_TAB_CHALLENGER)

    // 운영진 탭은 소속 칩의 chapterId·schoolId를 전달하지 않는다.
    // schoolId를 비워야 중앙 발신 공지와 서버가 허용한 교내 공지를 함께 조회할 수 있다.
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
    val selectedChipText: String = AppStrings.COMMON_ALL,
    val selectedPart: UserPart? = null,
    val noticeList: ImmutableList<NoticeSummary> = persistentListOf(),
    val currentPage: Int = 0,
    val isPageLoading: Boolean = false,
    /** 당겨서 새로고침 인디케이터 표시 여부 */
    val isRefreshing: Boolean = false,
    /** 조회 실패 사유. null이면 정상(결과가 0건일 수 있음) */
    val errorMessage: String? = null,
    val isLastPage: Boolean = false,
    /** 다음 페이지 요청이 실패했는지. true면 목록 끝에 다시 시도 버튼을 보인다 */
    val isNextPageError: Boolean = false,
    val canWriteNotice: Boolean = false,
    val canOpenAdminNotice: Boolean = false,
    val readNoticeIds: ImmutableSet<Long> = persistentSetOf(),
    val currentNoticeTab: String = NOTICE_TAB_CHALLENGER,
) : UiState {

    /**
     * 파트 필터 바텀시트에 올릴 목록.
     *
     * 11기부터 파트가 트랙 기준으로 바뀌어, 지금 보고 있는 기수에 맞는 목록만 보여준다.
     */
    val partFilters: List<UserPart>
        get() = UserPart.filtersOf(
            dropdownList
                .firstOrNull { it.gisuId.toLong() == selectedGisu }
                ?.generation
                ?: 0
        )
}

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
