package com.umc.presentation.notice.adminnotice

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.component.theme.AppStrings
import com.umc.domain.model.UserInfo
import com.umc.domain.model.enums.UserChallengerRole
import com.umc.domain.model.notice.noticeRolesForGisu
import com.umc.domain.model.notice.NoticeSummary
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 운영진 공지 탭. 권한 계층(중앙운영사무국 > 교내 회장단 > 파트장)에 따라
 * 자신의 레벨부터 하위 레벨까지의 탭만 노출됨.
 *
 * 서버 스펙상 noticeTab은 CHALLENGER(일반 공지) 외 CENTRAL_MEMBER / SCHOOL_CORE /
 * SCHOOL_PART_LEADER 세 가지이며, 자기 role보다 상위 tab 요청 시 403이 발생함.
 *
 * 지부장 전용 noticeTab은 없다. 서버는 CHAPTER_PRESIDENT를 SCHOOL_CORE로 매핑하므로
 * 지부장에게 학교 회장단·파트장 탭만 노출한다. CENTRAL_MEMBER 요청은 403이다.
 */
enum class AdminNoticeTab(
    val label: String,
    val noticeTab: String, // 공지 목록 조회 탭 (서버 스펙)
    val accessRoles: Set<UserChallengerRole>,
) {
    CENTRAL(
        label = AppStrings.ADMIN_NOTICE_CENTRAL,
        noticeTab = "CENTRAL_MEMBER",
        accessRoles = setOf(
            UserChallengerRole.CENTRAL_PRESIDENT,
            UserChallengerRole.CENTRAL_VICE_PRESIDENT,
            UserChallengerRole.CENTRAL_OPERATING_TEAM_MEMBER,
            UserChallengerRole.CENTRAL_EDUCATION_TEAM_MEMBER,
        ),
    ),
    SCHOOL_CORE(
        label = AppStrings.ADMIN_NOTICE_SCHOOL_CORE,
        noticeTab = "SCHOOL_CORE",
        accessRoles = setOf(
            UserChallengerRole.CHAPTER_PRESIDENT,
            UserChallengerRole.SCHOOL_PRESIDENT,
            UserChallengerRole.SCHOOL_VICE_PRESIDENT,
        ),
    ),
    PART_LEADER(
        label = AppStrings.ADMIN_NOTICE_PART_LEADER,
        noticeTab = "SCHOOL_PART_LEADER",
        accessRoles = setOf(UserChallengerRole.SCHOOL_PART_LEADER, UserChallengerRole.SCHOOL_ETC_ADMIN),
    );

    companion object {
        /** 사용자 role 중 가장 높은 운영진 레벨의 탭 반환. 운영진 권한이 없으면 null */
        fun highestAccessibleTab(roles: List<UserChallengerRole>): AdminNoticeTab? {
            return entries.firstOrNull { tab -> roles.any { it in tab.accessRoles } }
        }
    }
}

@HiltViewModel
class AdminNoticeViewModel @Inject constructor(
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val getNoticeListUseCase: GetNoticeListUseCase,
    private val appDataStoreRepository: AppDataStoreRepository,
) : BaseViewModel<AdminNoticeUiState, AdminNoticeEvent>(
    AdminNoticeUiState(),
) {
    private var cachedUserInfo: UserInfo? = null
    private var listJob: Job? = null

    init {
        loadUserAccess()
        collectReadNoticeIds()
    }

    /** 조회 대상 기수는 공지 화면에서 nav argument로 전달받아 주입 */
    fun setGisuId(gisuId: Long) {
        if (uiState.value.gisuId == gisuId && (gisuId > 0 || uiState.value.isAccessResolved)) return
        listJob?.cancel()
        updateState { resetAdminNoticeGisu(this, gisuId) }
        cachedUserInfo?.let(::updateUserAccess)
    }

    /** 읽은 공지 ID를 dataStore에서 구독해 상태에 반영 (읽지 않은 공지 빨간 점 표시용) */
    private fun collectReadNoticeIds() = viewModelScope.launch {
        appDataStoreRepository.getReadNoticeIds().collect { readIds ->
            updateState { copy(readNoticeIds = readIds.toImmutableSet()) }
        }
    }

    /** 사용자 role로 접근 가능한 탭 목록을 계산. 운영진 권한이 없으면 접근 불가 화면 노출 */
    private fun loadUserAccess() = viewModelScope.launch {
        getUserInfoUseCase().collect { userInfo ->
            cachedUserInfo = userInfo
            updateUserAccess(userInfo)
        }
    }

    private fun updateUserAccess(userInfo: UserInfo) {
        val next = resolveAdminNoticeAccess(uiState.value, userInfo)
        if (uiState.value.visibleTabs != next.visibleTabs || uiState.value.selectedTab != next.selectedTab) {
            listJob?.cancel()
        }
        updateState { next }
        refreshIfReady()
    }

    fun onClickTab(tab: AdminNoticeTab) {
        if (!uiState.value.hasAccess || tab !in uiState.value.visibleTabs) return
        if (tab == uiState.value.selectedTab) return
        // 진행 중이던 이전 탭 조회는 버리고, 이전 탭의 목록·페이징을 비워 새 탭 제목 아래에 남지 않게 한다.
        // 비워 두면 전환 중에는 로딩이, 조회 실패 시에는 실패 안내가 새 탭 기준으로 보인다
        listJob?.cancel()
        updateState {
            copy(
                selectedTab = tab,
                noticeList = persistentListOf(),
                currentPage = 0,
                isLastPage = false,
                isPageLoading = false,
                hasLoaded = false,
                errorMessage = null,
            )
        }
        refreshIfReady()
    }

    fun onClickSearch() = viewModelScope.launch {
        val state = uiState.value
        val selectedTab = state.selectedTab ?: return@launch
        if (!state.canQuery) return@launch

        // 검색에서도 학교를 지정하지 않아야 중앙 발신 공지가 검색 범위에서 빠지지 않는다.
        emitEvent(
            AdminNoticeEvent.MoveToSearchEvent(
                gisuId = state.gisuId,
                noticeTab = selectedTab.noticeTab,
                schoolId = null,
            )
        )
    }

    /** 공지 클릭 시 읽음 처리 후 상세로 이동 */
    fun onClickNotice(noticeId: Long) {
        viewModelScope.launch {
            appDataStoreRepository.addReadNoticeId(noticeId)
        }
        emitEvent(AdminNoticeEvent.MoveToDetailEvent(noticeId))
    }

    fun loadNextPage() {
        if (uiState.value.canQuery && !uiState.value.isPageLoading && !uiState.value.isLastPage) {
            getNoticeList(isRefresh = false)
        }
    }

    /** 기수·탭이 모두 준비된 경우에만 목록 새로고침 */
    private fun refreshIfReady() {
        val state = uiState.value
        if (state.canQuery) {
            getNoticeList(isRefresh = true)
        }
    }

    private fun getNoticeList(isRefresh: Boolean) {
        // 프로필이 갱신돼도 진행 중인 조회의 취소 핸들을 중복 작업으로 덮어쓰지 않는다.
        if (listJob?.isActive == true) return
        listJob = fetchNoticeList(isRefresh)
    }

    private fun fetchNoticeList(isRefresh: Boolean) = viewModelScope.launch {
        val state = uiState.value
        val selectedTab = state.selectedTab ?: return@launch

        if (!state.canQuery || state.isPageLoading || (!isRefresh && state.isLastPage)) return@launch

        updateState { copy(isPageLoading = true, errorMessage = null) }

        val pageToFetch = if (isRefresh) 0 else state.currentPage

        // 학교 미지정 시 서버가 역할별 조회 범위를 적용한다. 지부장은 schoolId 지정 조회가 403이다.
        val response = getNoticeListUseCase(
            gisuId = state.gisuId,
            noticeTab = selectedTab.noticeTab,
            schoolId = null,
            page = pageToFetch,
            size = 20
        )
        // apiCall이 취소를 실패 응답으로 바꿔도 이전 조회가 새 탭의 상태를 덮어쓰지 않게 한다.
        currentCoroutineContext().ensureActive()
        if (!uiState.value.canQuery || uiState.value.gisuId != state.gisuId ||
            uiState.value.selectedTab != selectedTab) return@launch
        resultResponse(
            response = response,
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
                        hasLoaded = true,
                        isLastPage = !noticeSearch.hasNext,
                    )
                }
            },
            errorCallback = { fail ->
                // 실패 사유를 남기지 않으면 "0건" 빈 상태와 구분되지 않는다
                updateState {
                    copy(isPageLoading = false, hasLoaded = true, errorMessage = fail.message)
                }
            }
        )
    }

}

data class AdminNoticeUiState(
    val gisuId: Long = 0,
    val hasAccess: Boolean = false,
    /** 미확정 동안에는 로딩을 표시하고 검색·조회 요청을 허용하지 않는다. */
    val isAccessResolved: Boolean = false,
    val visibleTabs: ImmutableList<AdminNoticeTab> = persistentListOf(),
    val selectedTab: AdminNoticeTab? = null,
    val noticeList: ImmutableList<NoticeSummary> = persistentListOf(),
    val currentPage: Int = 0,
    val isPageLoading: Boolean = false,
    val isLastPage: Boolean = false,
    val readNoticeIds: ImmutableSet<Long> = persistentSetOf(),
    // 첫 조회가 끝났는지 (끝나기 전의 빈 목록을 "0건"으로 오해하지 않기 위함)
    val hasLoaded: Boolean = false,
    val errorMessage: String? = null,
    val isNextPageError: Boolean = false,
) : UiState {
    val canQuery: Boolean
        get() = isAccessResolved && hasAccess && gisuId > 0 && selectedTab != null && selectedTab in visibleTabs

    // 접근 가능 + 첫 조회 완료 + 로딩 아님 + 실패 아님 + 목록 비어있음 -> 빈 상태 노출
    val isEmpty: Boolean
        get() = isAccessResolved && hasAccess && hasLoaded && !isPageLoading && errorMessage == null && noticeList.isEmpty()

    // 접근 가능 + 목록 비어있음 + 아직 조회 전이거나 조회 중 -> 로딩 표시
    val isLoadingEmpty: Boolean
        get() = hasAccess && noticeList.isEmpty() && errorMessage == null && (!hasLoaded || isPageLoading)
}

internal fun resetAdminNoticeGisu(state: AdminNoticeUiState, gisuId: Long): AdminNoticeUiState = state.copy(
    gisuId = gisuId,
    hasAccess = false,
    isAccessResolved = gisuId <= 0,
    visibleTabs = persistentListOf(),
    selectedTab = null,
    noticeList = persistentListOf(),
    currentPage = 0,
    isLastPage = false,
    isPageLoading = false,
    hasLoaded = gisuId <= 0,
    errorMessage = null,
    isNextPageError = false,
)

internal fun resolveAdminNoticeAccess(state: AdminNoticeUiState, userInfo: UserInfo): AdminNoticeUiState {
    val highestTab = if (state.gisuId <= 0) null
        else if (userInfo.isSuperAdmin) AdminNoticeTab.CENTRAL
        else AdminNoticeTab.highestAccessibleTab(userInfo.noticeRolesForGisu(state.gisuId))
    val tabs = highestTab?.let { highest -> AdminNoticeTab.entries.filter { it.ordinal >= highest.ordinal } }.orEmpty()
    val selected = state.selectedTab?.takeIf { it in tabs } ?: highestTab
    val accessChanged = state.visibleTabs != tabs || state.selectedTab != selected
    return state.copy(
        hasAccess = highestTab != null,
        // 프로필이 nav 기수보다 먼저 도착해도 초기 프레임을 권한 거부로 확정하지 않는다.
        isAccessResolved = state.gisuId > 0 || state.isAccessResolved,
        visibleTabs = tabs.toImmutableList(),
        selectedTab = selected,
        noticeList = if (accessChanged) persistentListOf() else state.noticeList,
        currentPage = if (accessChanged) 0 else state.currentPage,
        isLastPage = if (accessChanged) false else state.isLastPage,
        isPageLoading = if (accessChanged) false else state.isPageLoading,
        hasLoaded = if (accessChanged) false else state.hasLoaded,
        errorMessage = if (accessChanged) null else state.errorMessage,
        isNextPageError = if (accessChanged) false else state.isNextPageError,
    )
}

sealed interface AdminNoticeEvent : UiEvent {

    data class MoveToSearchEvent(
        val gisuId: Long,
        val noticeTab: String,
        val schoolId: Long?,
    ) : AdminNoticeEvent

    data class MoveToDetailEvent(val noticeId: Long) : AdminNoticeEvent
}
