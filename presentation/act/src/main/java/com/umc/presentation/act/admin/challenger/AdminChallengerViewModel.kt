package com.umc.presentation.act.admin.challenger

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.act.challenger.AdminChallenger
import com.umc.domain.model.act.challenger.ChallengerManageDialogModel
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.enums.PointType
import com.umc.domain.model.enums.PunishCategory
import com.umc.domain.model.enums.RewardType
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.request.challenger.ChallengerPointRequest
import com.umc.domain.usecase.challenger.DeleteChallengerPointUseCase
import com.umc.domain.usecase.challenger.GetAdminChallengerDetailUseCase
import com.umc.domain.usecase.challenger.GetAdminChallengerListUseCase
import com.umc.domain.usecase.challenger.GrantChallengerPointUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PAGE_SIZE = 100
private const val MAX_PAGES = 20

@HiltViewModel
class AdminChallengerViewModel @Inject constructor(
    private val getAdminChallengerListUseCase: GetAdminChallengerListUseCase, //관리자 챌린저 목록 조회
    private val getAdminChallengerDetailUseCase: GetAdminChallengerDetailUseCase, //관리자 챌린저 상세 조회
    private val grantChallengerPointUseCase: GrantChallengerPointUseCase, //챌린저 상벌점 부여
    private val deleteChallengerPointUseCase: DeleteChallengerPointUseCase, //챌린저 상벌점 기록 삭제
) : BaseViewModel<AdminChallengerUiState, AdminChallengerEvent>(
    AdminChallengerUiState()
) {
    private var challengerListJob: Job? = null

    //초기 챌린저 목록 조회
    //검색어 변경 시 첫 페이지부터 다시 조회
    fun onSearchKeywordChanged(keyword: String) {
        updateState { copy(searchKeyword = keyword) }
        getChallengers(
            keyword = keyword.trim().takeIf { it.isNotEmpty() },
            debounce = true
        )
    }

    fun refresh() {
        // 이미 목록이 있으면 탭 재진입 때 깜빡임 없이 조용히 갱신하고, 비었거나 오류였으면 로딩을 보인다.
        getChallengers(showLoading = uiState.value.sections.isEmpty())
    }

    fun openPartFilter() {
        updateState { copy(isPartFilterVisible = true) }
    }

    fun dismissPartFilter() {
        updateState { copy(isPartFilterVisible = false) }
    }

    fun selectPartFilter(part: UserPart) {
        val nextPart = part.takeUnless { uiState.value.selectedPart == it }
        updateState {
            copy(
                selectedPart = nextPart,
                isPartFilterVisible = false
            )
        }
        getChallengers(selectedPart = nextPart)
    }

    //관리자용 챌린저 목록 조회
    private fun getChallengers(
        keyword: String? = uiState.value.searchKeyword.trim().takeIf { it.isNotEmpty() },
        debounce: Boolean = false,
        selectedPart: UserPart? = uiState.value.selectedPart,
        showLoading: Boolean = true,
    ) {
        challengerListJob?.cancel()
        challengerListJob = viewModelScope.launch {
            if (debounce) delay(300)
            // 이전 검색 결과가 로딩·오류 화면 뒤에 남지 않도록 비운다. 조용한 갱신(탭 재진입)은 목록을 그대로 둔다.
            if (showLoading) {
                updateState { copy(loadState = AdminChallengerLoadState.LOADING, sections = persistentListOf()) }
            }
            startLoading()

            val response = fetchChallengers(keyword, selectedPart)
            // apiCall 이 CancellationException 을 Fail 로 삼키므로, 취소된 조회가 상태를 덮어쓰지 않게 한다.
            currentCoroutineContext().ensureActive()

            resultResponse(
                response = response,
                successCallback = { challengers ->
                    updateState {
                        copy(sections = challengers.toSections(), loadState = AdminChallengerLoadState.SUCCESS)
                    }
                },
                errorCallback = {
                    // 조용한 갱신이 실패하면 보이던 목록을 유지하고, 보여줄 목록이 없을 때만 오류 화면을 띄운다.
                    updateState {
                        if (sections.isEmpty()) copy(loadState = AdminChallengerLoadState.ERROR) else this
                    }
                }
            )
        }
    }

    //파트를 고르지 않으면 part 를 보내지 않는다. 서버가 part 를 주면 그 파트 행만 돌려주므로
    //파트가 없는 챌린저(회장·부회장 등)는 part 없이 조회해야 목록에 나온다.
    private suspend fun fetchChallengers(
        keyword: String?,
        selectedPart: UserPart?,
    ): ApiState<List<AdminChallenger>> {
        val challengers = mutableListOf<AdminChallenger>()
        var cursor: Long? = null
        repeat(MAX_PAGES) {
            val response = getAdminChallengerListUseCase(
                cursor = cursor,
                size = PAGE_SIZE,
                schoolId = null,
                gisuId = null,
                keyword = keyword,
                part = selectedPart?.name
            )
            when (response) {
                is ApiState.Fail -> return response
                is ApiState.Success -> {
                    challengers += response.data.challengers
                    cursor = response.data.nextCursor
                    if (!response.data.hasNext || cursor == null) return ApiState.Success(challengers.distinctBy { it.id })
                }
            }
        }
        return ApiState.Success(challengers.distinctBy { it.id })
    }

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
            resultResponse(
                response = getAdminChallengerDetailUseCase(challengerId),
                successCallback = { detail ->
                    updateState { copy(detail = detail, detailLoadState = AdminChallengerLoadState.SUCCESS) }
                },
                errorCallback = {
                    updateState { copy(detailLoadState = AdminChallengerLoadState.ERROR) }
                    emitEvent(AdminChallengerEvent.ShowToast(it.message))
                }
            )
        }
    }

    //상벌점 기록 수정 모드 전환
    fun toggleDetailEditMode() {
        updateState { copy(isDetailEditMode = !isDetailEditMode) }
    }

    //삭제할 상벌점 기록 선택
    fun selectDeleteTarget(pointId: Long?) {
        updateState { copy(deleteTarget = pointId) }
    }

    //선택된 상벌점 기록 삭제
    fun deleteSelectedPoint() {
        val targetId = uiState.value.deleteTarget ?: return
        // 응답 전에 '삭제하기'를 다시 눌러도 같은 DELETE 가 중복으로 나가지 않게 한다.
        if (uiState.value.isPointSubmitting) return
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
            emitEvent(AdminChallengerEvent.ShowToast("상점과 벌점을 동시에 입력할 수 없습니다."))
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
        if (challengerId <= 0L || uiState.value.isPointSubmitting) return
        updateState { copy(isPointSubmitting = true) }
        viewModelScope.launch {
            startLoading()
            resultResponse(
                response = grantChallengerPointUseCase(
                    id = challengerId,
                    request = ChallengerPointRequest(
                        pointType = pointType,
                        description = description,
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
    //파트별 챌린저 목록
    val sections: ImmutableList<AdminChallengerSectionUi> = persistentListOf(),
    //챌린저 목록 조회 상태
    val loadState: AdminChallengerLoadState = AdminChallengerLoadState.LOADING,
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
) : UiState

enum class AdminChallengerLoadState { LOADING, SUCCESS, ERROR }

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
        nicknameWithName = "$name($nickname)",
        generation = "${generation}기",
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
