package com.umc.presentation.notice.adminnotice

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.component.theme.AppStrings
import com.umc.domain.model.UserInfo
import com.umc.domain.model.enums.UserChallengerRole
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
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 운영진 공지 탭. 권한 계층(중앙운영사무국 > 교내 회장단 > 파트장)에 따라
 * 자신의 레벨부터 하위 레벨까지의 탭만 노출됨.
 *
 * 서버 스펙상 noticeTab은 CHALLENGER(일반 공지) 외 CENTRAL_MEMBER / SCHOOL_CORE /
 * SCHOOL_PART_LEADER 세 가지이며, 자기 role보다 상위 tab 요청 시 403이 발생함.
 *
 * 디자인의 "지부장 공지" 탭은 서버 noticeTab에 지부 등급이 없어 보류 상태다.
 * 지부장(CHAPTER_PRESIDENT)은 읽기만 중앙 레벨로 묶어 처리하고, 발행 권한은 막아둠
 * (NoticeWriterRole.CHAPTER_PRESIDENT 주석 참고).
 * 서버에 지부 등급 tab이 추가되면 CENTRAL과 SCHOOL_CORE 사이에 항목을 넣고
 * CENTRAL.accessRoles에서 CHAPTER_PRESIDENT를 옮기면 된다
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
            UserChallengerRole.SUPER_ADMIN,
            UserChallengerRole.CENTRAL_PRESIDENT,
            UserChallengerRole.CENTRAL_VICE_PRESIDENT,
            UserChallengerRole.CENTRAL_OPERATING_TEAM_MEMBER,
            UserChallengerRole.CENTRAL_EDUCATION_TEAM_MEMBER,
            UserChallengerRole.CHAPTER_PRESIDENT,
        ),
    ),
    SCHOOL_CORE(
        label = AppStrings.ADMIN_NOTICE_SCHOOL_CORE,
        noticeTab = "SCHOOL_CORE",
        accessRoles = setOf(
            UserChallengerRole.SCHOOL_PRESIDENT,
            UserChallengerRole.SCHOOL_VICE_PRESIDENT,
            UserChallengerRole.SCHOOL_ETC_ADMIN,
        ),
    ),
    PART_LEADER(
        label = AppStrings.ADMIN_NOTICE_PART_LEADER,
        noticeTab = "SCHOOL_PART_LEADER",
        accessRoles = setOf(UserChallengerRole.SCHOOL_PART_LEADER),
    );

    // 학교 소속 운영진 레벨 여부 (교내 운영진 공지는 schoolId로 구분됨)
    val isSchoolLevel: Boolean
        get() = this == SCHOOL_CORE || this == PART_LEADER

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
        // 기수를 알 수 없으면 조회가 시작되지 않으므로, 끝없는 로딩 대신 빈 상태로 떨어지게 한다
        updateState { copy(gisuId = gisuId, hasLoaded = hasLoaded || gisuId <= 0L) }
        refreshIfReady()
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
            val roles = userInfo.roles.map { UserChallengerRole.from(it.roleType) }
            val highestTab = AdminNoticeTab.highestAccessibleTab(roles)

            if (highestTab == null) {
                // 접근 권한 없음: 최하위 탭 칩만 노출하고 잠금 화면 표시
                updateState {
                    copy(
                        hasAccess = false,
                        visibleTabs = persistentListOf(AdminNoticeTab.PART_LEADER),
                        selectedTab = AdminNoticeTab.PART_LEADER,
                    )
                }
            } else {
                updateState {
                    copy(
                        hasAccess = true,
                        visibleTabs = AdminNoticeTab.entries
                            .filter { it.ordinal >= highestTab.ordinal }
                            .toImmutableList(),
                        selectedTab = selectedTab ?: highestTab,
                    )
                }
                refreshIfReady()
            }
        }
    }

    fun onClickTab(tab: AdminNoticeTab) {
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
        if (state.gisuId <= 0L) return@launch

        // 운영진 공지 검색도 목록과 같은 탭·schoolId 규칙으로 조회해야 한다
        val isSchoolStaff = state.visibleTabs.firstOrNull()?.isSchoolLevel == true
        emitEvent(
            AdminNoticeEvent.MoveToSearchEvent(
                gisuId = state.gisuId,
                noticeTab = selectedTab.noticeTab,
                schoolId = if (isSchoolStaff) findUserSchoolId() else null,
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
        if (!uiState.value.isPageLoading && !uiState.value.isLastPage) {
            getNoticeList(isRefresh = false)
        }
    }

    /** 기수·탭이 모두 준비된 경우에만 목록 새로고침 */
    private fun refreshIfReady() {
        val state = uiState.value
        if (state.gisuId != 0L && state.hasAccess && state.selectedTab != null) {
            getNoticeList(isRefresh = true)
        }
    }

    private fun getNoticeList(isRefresh: Boolean) {
        listJob = fetchNoticeList(isRefresh)
    }

    private fun fetchNoticeList(isRefresh: Boolean) = viewModelScope.launch {
        val state = uiState.value
        val selectedTab = state.selectedTab ?: return@launch

        if (state.isPageLoading || (!isRefresh && state.isLastPage)) return@launch

        updateState { copy(isPageLoading = true, errorMessage = null) }

        val pageToFetch = if (isRefresh) 0 else state.currentPage

        // 학교 소속 운영진(교내 회장단/파트장)은 schoolId를 함께 전달해 교내 운영진 공지를 조회하고,
        // 중앙 레벨은 schoolId 미입력으로 중앙운영사무국 공지를 조회 (서버 스펙: schoolId 입력 여부로 구분)
        val isSchoolStaff = state.visibleTabs.firstOrNull()?.isSchoolLevel == true
        val schoolId = if (isSchoolStaff) findUserSchoolId() else null

        resultResponse(
            response = getNoticeListUseCase(
                gisuId = state.gisuId,
                noticeTab = selectedTab.noticeTab,
                schoolId = schoolId,
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

    /** 선택된 기수 기준 사용자의 소속 학교 ID 조회 */
    private fun findUserSchoolId(): Long? {
        val gisuId = uiState.value.gisuId
        return cachedUserInfo?.challengerRecords
            ?.filter { gisuId == 0L || it.gisuId == gisuId }
            ?.firstOrNull { it.schoolId != 0L }
            ?.schoolId
    }
}

data class AdminNoticeUiState(
    val gisuId: Long = 0,
    val hasAccess: Boolean = true,
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
) : UiState {
    // 접근 가능 + 첫 조회 완료 + 로딩 아님 + 실패 아님 + 목록 비어있음 -> 빈 상태 노출
    val isEmpty: Boolean
        get() = hasAccess && hasLoaded && !isPageLoading && errorMessage == null && noticeList.isEmpty()

    // 접근 가능 + 목록 비어있음 + 아직 조회 전이거나 조회 중 -> 로딩 표시
    val isLoadingEmpty: Boolean
        get() = hasAccess && noticeList.isEmpty() && errorMessage == null && (!hasLoaded || isPageLoading)
}

sealed interface AdminNoticeEvent : UiEvent {

    data class MoveToSearchEvent(
        val gisuId: Long,
        val noticeTab: String,
        val schoolId: Long?,
    ) : AdminNoticeEvent

    data class MoveToDetailEvent(val noticeId: Long) : AdminNoticeEvent
}
