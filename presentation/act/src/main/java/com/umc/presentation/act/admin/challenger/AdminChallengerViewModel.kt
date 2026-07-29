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
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PAGE_SIZE = 30

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
        getChallengers()
    }

    fun openPartFilter() {
        updateState { copy(isPartFilterVisible = true) }
    }

    fun dismissPartFilter() {
        updateState { copy(isPartFilterVisible = false) }
    }

    fun selectPartFilter(part: UserPart) {
        updateState {
            copy(
                selectedPart = part,
                isPartFilterVisible = false
            )
        }
        getChallengers(selectedPart = part)
    }

    //관리자용 챌린저 목록 조회
    private fun getChallengers(
        keyword: String? = uiState.value.searchKeyword.trim().takeIf { it.isNotEmpty() },
        debounce: Boolean = false,
        selectedPart: UserPart? = uiState.value.selectedPart,
    ) {
        challengerListJob?.cancel()
        challengerListJob = viewModelScope.launch {
            if (debounce) delay(300)
            startLoading()

            val responses = coroutineScope {
                (selectedPart?.let(::listOf)
                    ?: UserPart.entries.filterNot { it == UserPart.UNKNOWN })
                    .map { part ->
                        async {
                            getAdminChallengerListUseCase(
                                cursor = null,
                                size = PAGE_SIZE,
                                schoolId = null,
                                gisuId = null,
                                keyword = keyword,
                                part = part.name
                            )
                        }
                    }
                    .awaitAll()
            }

            val challengers = responses
                .flatMap { response ->
                    var partChallengers = emptyList<AdminChallenger>()
                    resultResponse(
                        response = response,
                        successCallback = { partChallengers = it.challengers }
                    )
                    partChallengers
                }
                .distinctBy { it.id }

            updateState {
                copy(sections = challengers.toSections())
            }
            responses.filterIsInstance<ApiState.Fail>().firstOrNull()?.let {
                emitEvent(AdminChallengerEvent.ShowToast(it.failState.message))
            }
            stopLoading()
        }
    }

    //선택한 챌린저 상세 정보 조회
    fun getChallengerDetail(challengerId: Long) {
        if (challengerId <= 0L) return
        viewModelScope.launch {
            startLoading()
            resultResponse(
                response = getAdminChallengerDetailUseCase(challengerId),
                successCallback = { detail -> updateState { copy(detail = detail) } },
                errorCallback = { emitEvent(AdminChallengerEvent.ShowToast(it.message)) }
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
        viewModelScope.launch {
            startLoading()
            resultResponse(
                response = deleteChallengerPointUseCase(targetId),
                successCallback = {
                    updateState {
                        copy(
                            detail = detail?.copy(history = detail.history.filterNot { it.id == targetId }),
                            deleteTarget = null
                        )
                    }
                },
                errorCallback = { emitEvent(AdminChallengerEvent.ShowToast(it.message)) }
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
        grantPoint(challengerId, PointType.CUSTOM, uiState.value.customReason)
    }

    //상벌점 부여 공통 처리
    private fun grantPoint(challengerId: Long, pointType: PointType, description: String) {
        if (challengerId <= 0L) return
        viewModelScope.launch {
            startLoading()
            resultResponse(
                response = grantChallengerPointUseCase(
                    id = challengerId,
                    request = ChallengerPointRequest(pointType = pointType, description = description)
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
                            customReason = ""
                        )
                    }
                    emitEvent(AdminChallengerEvent.PointGranted)
                },
                errorCallback = { emitEvent(AdminChallengerEvent.ShowToast(it.message)) }
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
    val sections: List<AdminChallengerSectionUi> = emptyList(),
    //선택한 챌린저 상세 정보
    val detail: ChallengerManageDialogModel? = null,
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
) : UiState

data class AdminChallengerSectionUi(
    val partName: String,
    val members: List<AdminChallengerMemberUi>,
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

private fun List<AdminChallenger>.toSections(): List<AdminChallengerSectionUi> {
    return groupBy { it.part }
        .toSortedMap(compareBy<UserPart> { UserPart.entries.indexOf(it) })
        .map { (part, members) ->
            AdminChallengerSectionUi(
                partName = part.label,
                members = members.map { it.toMemberUi() }
            )
        }
}

//RewardType을 상벌점 부여 요청용 PointType으로 변환
private fun RewardType.toPointType(): PointType {
    return runCatching { PointType.valueOf(name) }.getOrDefault(PointType.CUSTOM)
}
