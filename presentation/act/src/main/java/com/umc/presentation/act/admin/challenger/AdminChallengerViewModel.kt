package com.umc.presentation.act.admin.challenger

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.component.theme.AppStrings
import com.umc.domain.model.UserInfo
import com.umc.domain.model.act.challenger.AdminChallenger
import com.umc.domain.model.act.challenger.ChallengerManageDialogModel
import com.umc.domain.model.act.challenger.ChallengerPointPermission
import com.umc.domain.model.act.challenger.challengerPointPermission
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.FailState
import com.umc.domain.model.act.challenger.ChallengerManagementFilters
import com.umc.domain.model.act.challenger.searchableChallengerGisus
import com.umc.domain.model.act.challenger.initialChallengerGisu
import com.umc.domain.model.organization.GisuItem
import com.umc.domain.usecase.organization.GetSearchGisusUseCase
import com.umc.domain.usecase.organization.GetChapterWithSchoolUseCase
import com.umc.domain.model.enums.PointType
import com.umc.domain.model.enums.PunishCategory
import com.umc.domain.model.enums.RewardType
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.request.challenger.ChallengerPointRequest
import com.umc.domain.usecase.appDataStore.GetUserInfoUseCase
import com.umc.domain.usecase.challenger.DeleteChallengerPointUseCase
import com.umc.domain.usecase.challenger.GetAdminChallengerDetailUseCase
import com.umc.domain.usecase.challenger.GetAdminChallengerListUseCase
import com.umc.domain.usecase.challenger.GrantChallengerPointUseCase
import com.umc.domain.usecase.member.GetMyProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PAGE_SIZE = 100
private const val MAX_PAGES = 20

// 서버 Grant/EditChallengerPointRequest 의 @Size(max = 200). Java String 길이(UTF-16)와 Kotlin length 가 같다
internal const val POINT_DESCRIPTION_MAX_LENGTH = 200

@HiltViewModel
class AdminChallengerViewModel @Inject constructor(
    private val getAdminChallengerListUseCase: GetAdminChallengerListUseCase, //관리자 챌린저 목록 조회
    private val getAdminChallengerDetailUseCase: GetAdminChallengerDetailUseCase, //관리자 챌린저 상세 조회
    private val grantChallengerPointUseCase: GrantChallengerPointUseCase, //챌린저 상벌점 부여
    private val deleteChallengerPointUseCase: DeleteChallengerPointUseCase, //챌린저 상벌점 기록 삭제
    private val getUserInfoUseCase: GetUserInfoUseCase, //저장된 내 정보(역할) 조회
    private val getMyProfileUseCase: GetMyProfileUseCase, //목록·상세 권한을 최신 프로필로 확인
    private val getSearchGisusUseCase: GetSearchGisusUseCase,
    private val getChapterWithSchoolUseCase: GetChapterWithSchoolUseCase,
) : BaseViewModel<AdminChallengerUiState, AdminChallengerEvent>(
    AdminChallengerUiState()
) {
    private var challengerListJob: Job? = null
    private var filterContextJob: Job? = null
    private var searchUserInfo: UserInfo? = null
    private var lastAdminModeSession: Int? = null

    //검색어 변경 시 첫 페이지부터 다시 조회
    fun onSearchKeywordChanged(keyword: String) {
        if (!uiState.value.filtersReady) return
        updateState { copy(searchKeyword = keyword) }
        getChallengers(
            keyword = keyword.trim().takeIf { it.isNotEmpty() },
            debounce = true
        )
    }

    /**
     * 탭 재진입·재시도 시 최신 프로필로 검색 가능한 기수와 소속 범위를 다시 구성한다.
     * 이전 기수·조직·파트 선택은 새 범위에서 유효할 때 유지하고 검색어도 보존한다.
     * 어드민 모드를 다시 켠 뒤 처음 진입할 때는 이전 선택과 검색어를 초기화한다.
     * 이전 요청과 목록을 정리해 다른 조회 조건의 결과가 화면에 남지 않게 한다.
     */
    fun refresh(adminModeSession: Int? = null) {
        if (adminModeSession != null && lastAdminModeSession != adminModeSession) {
            lastAdminModeSession = adminModeSession
            filterContextJob?.cancel()
            challengerListJob?.cancel()
            stopLoading()
            searchUserInfo = null
            updateState {
                copy(
                    searchKeyword = "",
                    selectedPart = null,
                    isPartFilterVisible = false,
                    searchableGisus = persistentListOf(),
                    selectedGisu = null,
                    searchFilters = null,
                    sections = persistentListOf(),
                    loadErrorMessage = null,
                )
            }
        }
        initializeSearch()
    }

    private fun initializeSearch(retryList: Boolean = true, deniedMessage: String? = null) {
        val previousFilters = uiState.value.searchFilters
        val preferredGisuId = uiState.value.selectedGisu?.gisuId?.toLong()
        filterContextJob?.cancel()
        challengerListJob?.cancel()
        stopLoading()
        updateState {
            copy(
                manageAccess = AdminChallengerAccess.CHECKING,
                filterLoadState = AdminChallengerLoadState.LOADING,
                searchFilters = null,
                sections = persistentListOf(),
                isPartFilterVisible = false,
            )
        }
        filterContextJob = viewModelScope.launch {
            // 최신 프로필로 검색 후보 범위를 구성한다. v2 검색 API가 최종 권한을 검사하므로 실패 시 캐시 권한으로 목록을 열지 않는다.
            val profile = getMyProfileUseCase()
            currentCoroutineContext().ensureActive()
            if (profile is ApiState.Fail) {
                initializationFailed(profile.failState)
                return@launch
            }
            val user = (profile as ApiState.Success).data
            if (user.id <= 0) {
                initializationFailed(FailState(code = "PROFILE_INVALID", message = AppStrings.CHALLENGER_LIST_LOAD_ERROR_DESCRIPTION))
                return@launch
            }
            searchUserInfo = user
            val response = getSearchGisusUseCase()
            currentCoroutineContext().ensureActive()
            if (response is ApiState.Fail) {
                initializationFailed(response.failState)
                return@launch
            }
            val gisus = user.searchableChallengerGisus((response as ApiState.Success).data.gisuList)
            val selected = gisus.initialChallengerGisu(preferredGisuId)
            if (selected == null) {
                updateState {
                    copy(
                        manageAccess = AdminChallengerAccess.DENIED,
                        searchableGisus = persistentListOf(),
                        selectedGisu = null,
                        searchFilters = null,
                    )
                }
                return@launch
            }
            updateState {
                copy(
                    manageAccess = AdminChallengerAccess.ALLOWED,
                    searchableGisus = gisus.toImmutableList(),
                    selectedGisu = selected,
                    selectedPart = selectedPart?.takeIf { it in UserPart.filtersOf(selected.generation) },
                )
            }
            loadOrganization(selected, previousFilters, retryList, deniedMessage)
        }
    }

    private fun initializationFailed(failure: FailState) {
        resultResponse<Unit>(ApiState.Fail(failure), successCallback = {}, errorCallback = {
            updateState {
                copy(
                    manageAccess = AdminChallengerAccess.ERROR,
                    filterLoadState = AdminChallengerLoadState.ERROR,
                    loadState = AdminChallengerLoadState.ERROR,
                    loadErrorMessage = it.message.takeIf { message -> message.isNotBlank() },
                )
            }
        })
    }

    private suspend fun loadOrganization(
        gisu: GisuItem,
        previous: ChallengerManagementFilters? = null,
        retryList: Boolean = true,
        deniedMessage: String? = null,
    ) {
        val response = getChapterWithSchoolUseCase(gisu.gisuId)
        currentCoroutineContext().ensureActive()
        if (response is ApiState.Fail) {
            organizationFailed(response.failState)
            return
        }
        val user = searchUserInfo ?: return
        var filters = ChallengerManagementFilters.create(user, gisu, (response as ApiState.Success).data)
        if (filters == null) {
            organizationFailed(FailState(
                code = "SCHOOL_CHAPTER_NOT_FOUND",
                message = AppStrings.CHALLENGER_SCHOOL_CHAPTER_NOT_FOUND,
            ))
            return
        }
        // 권한 범위가 바뀌었으면 이전 조직 선택을 버리고, 같은 범위에서도 최신 선택지에 있는 값만 복원한다.
        if (previous?.gisu?.gisuId == gisu.gisuId && previous.isOrganizationFixed == filters.isOrganizationFixed) {
            filters = filters.selectChapter(previous.chapterId).selectSchool(previous.schoolId)
        }
        updateState {
            copy(
                searchFilters = filters,
                filterLoadState = AdminChallengerLoadState.SUCCESS,
                loadState = if (retryList) AdminChallengerLoadState.LOADING else AdminChallengerLoadState.ERROR,
                loadErrorMessage = deniedMessage,
            )
        }
        // 403 직후에는 갱신된 권한만 보여 주고, 사용자가 재시도할 때 검색한다.
        if (retryList) getChallengers()
    }

    private fun organizationFailed(failure: FailState) {
        resultResponse<Unit>(ApiState.Fail(failure), successCallback = {}, errorCallback = {
            updateState {
                copy(
                    filterLoadState = AdminChallengerLoadState.ERROR,
                    loadState = AdminChallengerLoadState.ERROR,
                    loadErrorMessage = it.message.takeIf { message -> message.isNotBlank() },
                    sections = persistentListOf(),
                )
            }
        })
    }

    fun selectFilter(type: AdminChallengerFilterType, option: AdminChallengerFilterOption?) {
        if (uiState.value.manageAccess != AdminChallengerAccess.ALLOWED) return
        if (type == AdminChallengerFilterType.GISU) {
            val gisu = uiState.value.searchableGisus.firstOrNull { it.gisuId.toLong() == option?.id } ?: return
            if (gisu.gisuId == uiState.value.selectedGisu?.gisuId) return
            // 이전 기수의 조직 조회와 목록 요청을 취소하고, 새 기수의 범위가 준비될 때까지 기존 결과를 비운다.
            filterContextJob?.cancel()
            challengerListJob?.cancel()
            stopLoading()
            updateState {
                copy(
                    selectedGisu = gisu,
                    selectedPart = selectedPart?.takeIf { it in UserPart.filtersOf(gisu.generation) },
                    searchFilters = null,
                    filterLoadState = AdminChallengerLoadState.LOADING,
                    loadState = AdminChallengerLoadState.LOADING,
                    loadErrorMessage = null,
                    sections = persistentListOf(),
                    isPartFilterVisible = false,
                )
            }
            filterContextJob = viewModelScope.launch { loadOrganization(gisu) }
            return
        }
        val current = uiState.value.searchFilters ?: return
        val next = when (type) {
            AdminChallengerFilterType.CHAPTER -> current.selectChapter(option?.id)
            AdminChallengerFilterType.SCHOOL -> current.selectSchool(option?.id)
            AdminChallengerFilterType.GISU -> return
        }
        if (next == current) return
        updateState { copy(searchFilters = next) }
        getChallengers()
    }

    //내 정보. preferServer 면 서버 프로필을 먼저 받고(성공하면 저장 정보도 갱신된다), 실패하면 저장 정보를 쓴다.
    //로그아웃하면 id 만 지워지고 역할은 남으므로 저장 정보는 id 로 유효한지 본다.
    private suspend fun loadMyInfo(preferServer: Boolean = false): UserInfo? {
        if (preferServer) {
            (getMyProfileUseCase() as? ApiState.Success)?.data?.let { return it }
        }
        val cached = getUserInfoUseCase().first()
        if (cached.id > 0L) return cached
        if (preferServer) return null
        return (getMyProfileUseCase() as? ApiState.Success)?.data
    }

    fun openPartFilter() {
        if (!uiState.value.filtersReady) return
        updateState { copy(isPartFilterVisible = true) }
    }

    fun dismissPartFilter() {
        updateState { copy(isPartFilterVisible = false) }
    }

    fun selectPartFilter(part: UserPart) {
        if (!uiState.value.filtersReady || part !in UserPart.filtersOf(uiState.value.selectedGisu?.generation ?: 0)) return
        val nextPart = part.takeUnless { uiState.value.selectedPart == it }
        updateState {
            copy(
                selectedPart = nextPart,
                isPartFilterVisible = false
            )
        }
        getChallengers(selectedPart = nextPart)
    }

    // 검색 조건은 요청 시작 시 캡처해 페이지 사이에 기수나 조직 조건이 섞이지 않게 한다.
    private fun getChallengers(
        keyword: String? = uiState.value.searchKeyword.trim().takeIf { it.isNotEmpty() },
        debounce: Boolean = false,
        selectedPart: UserPart? = uiState.value.selectedPart,
    ) {
        val filters = uiState.value.searchFilters ?: return
        if (!uiState.value.filtersReady) return
        challengerListJob?.cancel()
        updateState {
            copy(loadState = AdminChallengerLoadState.LOADING, loadErrorMessage = null, sections = persistentListOf())
        }
        challengerListJob = viewModelScope.launch {
            if (debounce) delay(300)
            startLoading()
            val response = fetchChallengers(filters, keyword, selectedPart) { page ->
                updateState { copy(sections = page.toSections(), loadState = AdminChallengerLoadState.LOADING) }
            }
            // apiCall이 취소를 Fail로 바꿔도 이전 조건의 결과를 반영하지 않는다.
            currentCoroutineContext().ensureActive()
            resultResponse(
                response = response,
                successCallback = { challengers ->
                    updateState { copy(sections = challengers.toSections(), loadState = AdminChallengerLoadState.SUCCESS, loadErrorMessage = null) }
                },
                errorCallback = { failure ->
                    if (failure.code == "403" || failure.code.endsWith("-403")) {
                        // 서버에서 역할 변경을 감지하면 기존 선택 범위를 폐기한다. 자동 재요청은 하지 않는다.
                        initializeSearch(retryList = false, deniedMessage = failure.message)
                    } else {
                        updateState { copy(loadState = AdminChallengerLoadState.ERROR, loadErrorMessage = failure.message) }
                    }
                },
            )
        }
    }

    // 파트 미선택은 null로 전송해야 파트가 없는 회장·부회장도 검색 결과에 포함된다.
    private suspend fun fetchChallengers(
        filters: ChallengerManagementFilters,
        keyword: String?,
        selectedPart: UserPart?,
        onPage: (List<AdminChallenger>) -> Unit,
    ): ApiState<List<AdminChallenger>> {
        val challengers = mutableListOf<AdminChallenger>()
        val seenCursors = mutableSetOf<Long>()
        var cursor: Long? = null
        repeat(MAX_PAGES) {
            val response = getAdminChallengerListUseCase(
                cursor = cursor,
                size = PAGE_SIZE,
                schoolId = filters.schoolId,
                chapterId = filters.chapterId,
                gisuId = filters.gisu.gisuId.toLong(),
                keyword = keyword,
                part = selectedPart?.name,
            )
            currentCoroutineContext().ensureActive()
            when (response) {
                is ApiState.Fail -> return response
                is ApiState.Success -> {
                    if (response.data.hasNext && response.data.challengers.isEmpty()) return incompleteChallengerList()
                    challengers += response.data.challengers
                    onPage(challengers.distinctBy { it.id })
                    if (!response.data.hasNext) return ApiState.Success(challengers.distinctBy { it.id })
                    val next = response.data.nextCursor
                    if (next == null || !seenCursors.add(next)) return incompleteChallengerList()
                    cursor = next
                }
            }
        }
        return incompleteChallengerList()
    }

    private fun incompleteChallengerList(): ApiState.Fail = ApiState.Fail(
        FailState(code = "CHALLENGER_LIST_INCOMPLETE", message = AppStrings.CHALLENGER_LIST_INCOMPLETE)
    )

    //선택한 챌린저 상세 정보 조회
    fun getChallengerDetail(challengerId: Long) {
        if (challengerId <= 0L) {
            // 조회할 수 없는 id 라 영구 로딩이 되지 않도록 바로 오류 상태로 둔다.
            updateState { copy(detailLoadState = AdminChallengerLoadState.ERROR) }
            return
        }
        viewModelScope.launch {
            updateState { copy(detailLoadState = AdminChallengerLoadState.LOADING) }
            startLoading()
            val myInfo = loadMyInfo()
            resultResponse(
                response = getAdminChallengerDetailUseCase(challengerId),
                successCallback = { detail ->
                    updateState {
                        copy(
                            detail = detail,
                            detailLoadState = AdminChallengerLoadState.SUCCESS,
                            pointPermission = myInfo.pointPermissionFor(detail),
                        )
                    }
                },
                errorCallback = {
                    updateState { copy(detailLoadState = AdminChallengerLoadState.ERROR) }
                    emitEvent(AdminChallengerEvent.ShowToast(it.message))
                }
            )
        }
    }

    //상벌점 기록 수정 모드 전환
    //지금은 수정 모드에서 삭제만 할 수 있어 삭제 권한이 있을 때만 켠다.
    fun toggleDetailEditMode() {
        if (!uiState.value.pointPermission.canDelete) return
        updateState { copy(isDetailEditMode = !isDetailEditMode) }
    }

    //삭제할 상벌점 기록 선택
    fun selectDeleteTarget(pointId: Long?) {
        if (pointId != null && !uiState.value.pointPermission.canDelete) return
        updateState { copy(deleteTarget = pointId) }
    }

    //선택된 상벌점 기록 삭제
    fun deleteSelectedPoint() {
        val targetId = uiState.value.deleteTarget ?: return
        // 응답 전에 '삭제하기'를 다시 눌러도 같은 DELETE 가 중복으로 나가지 않게 한다.
        if (uiState.value.isPointSubmitting || !uiState.value.pointPermission.canDelete) return
        val challengerId = uiState.value.detail?.challengerId
        updateState { copy(isPointSubmitting = true) }
        viewModelScope.launch {
            startLoading()
            resultResponse(
                response = deleteChallengerPointUseCase(targetId),
                successCallback = {
                    updateState {
                        copy(
                            detail = detail?.copy(history = detail.history.filterNot { it.id == targetId }),
                            deleteTarget = null,
                            isPointSubmitting = false
                        )
                    }
                    // 총점수·상점/벌점 칩은 서버 계산값이라 삭제 응답(Unit)으로는 갱신되지 않으므로 상세를 다시 조회한다.
                    challengerId?.let(::getChallengerDetail)
                },
                errorCallback = {
                    updateState { copy(isPointSubmitting = false) }
                    emitEvent(AdminChallengerEvent.ShowToast(it.message))
                }
            )
        }
    }

    //부여할 보상 항목 선택
    fun selectReward(type: RewardType) {
        updateState { copy(selectedRewardType = type) }
    }

    //벌점 필터 선택
    fun selectPenaltyFilter(filter: PunishCategory) {
        updateState {
            copy(
                selectedPenaltyFilter = filter,
                selectedPenaltyType = selectedPenaltyType?.takeIf { filter == PunishCategory.ALL || it.category == filter }
            )
        }
    }

    //부여할 벌점 항목 선택
    fun selectPenalty(type: RewardType) {
        updateState { copy(selectedPenaltyType = type) }
    }

    //상벌점 메모 입력
    fun onMemoChanged(memo: String) {
        updateState { copy(pointMemo = memo) }
    }

    //기타 상벌점 사유 입력
    fun onCustomReasonChanged(reason: String) {
        updateState { copy(customReason = reason) }
    }

    fun resetPointGrantForm() {
        updateState {
            copy(
                selectedRewardType = null,
                selectedPenaltyFilter = PunishCategory.ALL,
                selectedPenaltyType = null,
                pointMemo = "",
                customRewardScore = 0,
                customPunishScore = 0,
                customReason = "",
            )
        }
    }

    fun increaseRewardScore() {
        updateState { copy(customRewardScore = customRewardScore + 1) }
    }

    fun decreaseRewardScore() {
        updateState { copy(customRewardScore = (customRewardScore - 1).coerceAtLeast(0)) }
    }

    fun increasePunishScore() {
        updateState { copy(customPunishScore = customPunishScore + 1) }
    }

    fun decreasePunishScore() {
        updateState { copy(customPunishScore = (customPunishScore - 1).coerceAtLeast(0)) }
    }

    //선택한 보상 항목 부여
    fun grantReward(challengerId: Long) {
        val selected = uiState.value.selectedRewardType ?: return
        grantPoint(challengerId, selected.toPointType(), uiState.value.pointMemo)
    }

    //선택한 벌점 항목 부여
    fun grantPenalty(challengerId: Long) {
        val selected = uiState.value.selectedPenaltyType ?: return
        grantPoint(challengerId, selected.toPointType(), uiState.value.pointMemo)
    }

    //기타 상벌점 부여
    fun grantCustomPoint(challengerId: Long) {
        val state = uiState.value
        if (state.customRewardScore > 0 && state.customPunishScore > 0) {
            emitEvent(AdminChallengerEvent.ShowToast(AppStrings.CHALLENGER_POINTS_CONFLICT_TOAST))
            return
        }

        val pointValue = when {
            state.customRewardScore > 0 -> state.customRewardScore
            state.customPunishScore > 0 -> -state.customPunishScore
            else -> return
        }
        grantPoint(challengerId, PointType.CUSTOM, state.customReason, pointValue)
    }

    //상벌점 부여 공통 처리
    private fun grantPoint(
        challengerId: Long,
        pointType: PointType,
        description: String,
        pointValue: Int? = null,
    ) {
        // 응답 전에 '등록하기'를 다시 눌러도 같은 POST 가 중복으로 나가지 않게 한다.
        if (challengerId <= 0L || uiState.value.isPointSubmitting || !uiState.value.pointPermission.canGrant) return
        // 서버는 사유가 비었거나 200자를 넘으면 400 을 준다. 앞뒤 공백은 잘라 보낸다.
        val reason = description.trim()
        if (reason.isEmpty() || reason.length > POINT_DESCRIPTION_MAX_LENGTH) return
        updateState { copy(isPointSubmitting = true) }
        viewModelScope.launch {
            startLoading()
            resultResponse(
                response = grantChallengerPointUseCase(
                    id = challengerId,
                    request = ChallengerPointRequest(
                        pointType = pointType,
                        description = reason,
                        pointValue = pointValue,
                    )
                ),
                successCallback = { detail ->
                    updateState {
                        copy(
                            detail = detail,
                            selectedRewardType = null,
                            selectedPenaltyType = null,
                            pointMemo = "",
                            customRewardScore = 0,
                            customPunishScore = 0,
                            customReason = "",
                            isPointSubmitting = false
                        )
                    }
                    emitEvent(AdminChallengerEvent.PointGranted)
                },
                errorCallback = {
                    updateState { copy(isPointSubmitting = false) }
                    emitEvent(AdminChallengerEvent.ShowToast(it.message))
                }
            )
        }
    }
}

data class AdminChallengerUiState(
    //검색어
    val searchKeyword: String = "",
    val selectedPart: UserPart? = null,
    val isPartFilterVisible: Boolean = false,
    val searchableGisus: ImmutableList<GisuItem> = persistentListOf(),
    val selectedGisu: GisuItem? = null,
    val searchFilters: ChallengerManagementFilters? = null,
    val filterLoadState: AdminChallengerLoadState = AdminChallengerLoadState.LOADING,
    //파트별 챌린저 목록
    val sections: ImmutableList<AdminChallengerSectionUi> = persistentListOf(),
    //챌린저 목록 조회 상태
    val loadState: AdminChallengerLoadState = AdminChallengerLoadState.LOADING,
    //목록 조회 실패 원인 문구(없으면 기본 안내)
    val loadErrorMessage: String? = null,
    //선택한 챌린저 상세 정보
    val detail: ChallengerManageDialogModel? = null,
    //챌린저 상세 조회 상태
    val detailLoadState: AdminChallengerLoadState = AdminChallengerLoadState.LOADING,
    //상세 화면 상벌점 수정 모드
    val isDetailEditMode: Boolean = false,
    //삭제 대상 상벌점 기록 ID
    val deleteTarget: Long? = null,
    //선택한 보상 타입
    val selectedRewardType: RewardType? = null,
    //선택한 벌점 필터
    val selectedPenaltyFilter: PunishCategory = PunishCategory.ALL,
    //선택한 벌점 타입
    val selectedPenaltyType: RewardType? = null,
    //보상/벌점 메모
    val pointMemo: String = "",
    //기타 보상 점수
    val customRewardScore: Int = 0,
    //기타 벌점 점수
    val customPunishScore: Int = 0,
    //기타 상벌점 사유
    val customReason: String = "",
    //상벌점 등록/삭제 요청 진행 중 여부
    val isPointSubmitting: Boolean = false,
    //상세 화면 대상 챌린저에 대한 내 상벌점 권한
    val pointPermission: ChallengerPointPermission = ChallengerPointPermission.NONE,
    //챌린저 관리 탭 사용 가능 여부
    val manageAccess: AdminChallengerAccess = AdminChallengerAccess.CHECKING,
) : UiState {
    val filtersReady: Boolean get() = manageAccess == AdminChallengerAccess.ALLOWED &&
        filterLoadState == AdminChallengerLoadState.SUCCESS && searchFilters != null
}

enum class AdminChallengerLoadState { LOADING, SUCCESS, ERROR }

enum class AdminChallengerAccess { CHECKING, ALLOWED, DENIED, ERROR }

//상세 조회가 성공했다면 서버가 조회를 허용한 것이므로 부여·사유 수정도 허용된다. 삭제만 역할로 판정한다.
private fun UserInfo?.pointPermissionFor(detail: ChallengerManageDialogModel): ChallengerPointPermission =
    (this?.challengerPointPermission(detail.gisuId, detail.schoolId) ?: ChallengerPointPermission.NONE)
        .withServerReadAllowed()

data class AdminChallengerSectionUi(
    val partName: String,
    val members: ImmutableList<AdminChallengerMemberUi>,
)

data class AdminChallengerMemberUi(
    val id: Long,
    val nicknameWithName: String,
    val generation: String,
    val totalScore: Int,
)

sealed interface AdminChallengerEvent : UiEvent {
    //상벌점 부여 완료
    data object PointGranted : AdminChallengerEvent
    //토스트 표시
    data class ShowToast(val message: String) : AdminChallengerEvent
}

//도메인 모델을 목록 UI 모델로 변환
private fun AdminChallenger.toMemberUi(): AdminChallengerMemberUi {
    return AdminChallengerMemberUi(
        id = id.toLong(),
        nicknameWithName = AppStrings.CHALLENGER_NAME_NICKNAME_FORMAT.format(nickname, name),
        generation = AppStrings.CHALLENGER_INFO_FORMAT_GENERATION.format(generation),
        totalScore = warningCount + outCount
    )
}

private fun List<AdminChallenger>.toSections(): ImmutableList<AdminChallengerSectionUi> {
    return groupBy { it.part }
        .toSortedMap(compareBy<UserPart> { UserPart.entries.indexOf(it) })
        .map { (part, members) ->
            AdminChallengerSectionUi(
                partName = part.label,
                members = members.map { it.toMemberUi() }.toImmutableList()
            )
        }
        .toImmutableList()
}

//RewardType을 상벌점 부여 요청용 PointType으로 변환
private fun RewardType.toPointType(): PointType {
    return runCatching { PointType.valueOf(name) }.getOrDefault(PointType.CUSTOM)
}
