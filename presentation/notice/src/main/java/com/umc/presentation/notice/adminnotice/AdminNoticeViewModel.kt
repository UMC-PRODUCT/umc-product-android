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

    init {
        loadUserAccess()
        collectReadNoticeIds()
    }

    /** 조회 대상 기수는 공지 화면에서 nav argument로 전달받아 주입 */
    fun setGisuId(gisuId: Long) {
        updateState { copy(gisuId = gisuId) }
        refreshIfReady()
    }

    /** 읽은 공지 ID를 dataStore에서 구독해 상태에 반영 (읽지 않은 공지 빨간 점 표시용) */
    private fun collectReadNoticeIds() = viewModelScope.launch {
        appDataStoreRepository.getReadNoticeIds().collect { readIds ->
            updateState { copy(readNoticeIds = readIds) }
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
                        visibleTabs = listOf(AdminNoticeTab.PART_LEADER),
                        selectedTab = AdminNoticeTab.PART_LEADER,
                    )
                }
            } else {
                updateState {
                    copy(
                        hasAccess = true,
                        visibleTabs = AdminNoticeTab.entries.filter { it.ordinal >= highestTab.ordinal },
                        selectedTab = selectedTab ?: highestTab,
                    )
                }
                refreshIfReady()
            }
        }
    }

    fun onClickTab(tab: AdminNoticeTab) {
        updateState { copy(selectedTab = tab) }
        refreshIfReady()
    }

    fun onClickSearch() {
        emitEvent(AdminNoticeEvent.MoveToSearchEvent(uiState.value.gisuId))
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

    private fun getNoticeList(isRefresh: Boolean) = viewModelScope.launch {
        val state = uiState.value
        val selectedTab = state.selectedTab ?: return@launch

        if (state.isPageLoading || (!isRefresh && state.isLastPage)) return@launch

        updateState { copy(isPageLoading = true) }

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
    val visibleTabs: List<AdminNoticeTab> = emptyList(),
    val selectedTab: AdminNoticeTab? = null,
    val noticeList: List<NoticeSummary> = emptyList(),
    val currentPage: Int = 0,
    val isPageLoading: Boolean = false,
    val isLastPage: Boolean = false,
    val readNoticeIds: Set<Long> = emptySet(),
) : UiState {
    // 접근 가능 + 로딩 아님 + 목록 비어있음 -> 빈 상태 노출
    val isEmpty: Boolean
        get() = hasAccess && !isPageLoading && noticeList.isEmpty()
}

sealed interface AdminNoticeEvent : UiEvent {

    data class MoveToSearchEvent(val gisuId: Long) : AdminNoticeEvent

    data class MoveToDetailEvent(val noticeId: Long) : AdminNoticeEvent
}
