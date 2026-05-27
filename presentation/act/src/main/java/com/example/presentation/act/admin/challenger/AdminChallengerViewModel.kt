package com.example.presentation.act.admin.challenger

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.act.challenger.AdminChallenger
import com.umc.domain.model.act.challenger.ChallengerManageDialogModel
import com.umc.domain.model.enums.PointType
import com.umc.domain.model.enums.PunishCategory
import com.umc.domain.model.enums.RewardType
import com.umc.domain.model.request.challenger.ChallengerPointRequest
import com.umc.domain.usecase.challenger.DeleteChallengerPointUseCase
import com.umc.domain.usecase.challenger.GetAdminChallengerDetailUseCase
import com.umc.domain.usecase.challenger.GetAdminChallengerListUseCase
import com.umc.domain.usecase.challenger.GrantChallengerPointUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
    //초기 챌린저 목록 조회
    init {
        getChallengers()
    }

    //검색어 변경 시 첫 페이지부터 다시 조회
    fun onSearchKeywordChanged(keyword: String) {
        updateState { copy(searchKeyword = keyword) }
        getChallengers(keyword = keyword.trim().takeIf { it.isNotEmpty() })
    }

    //관리자용 챌린저 목록 조회
    fun getChallengers(
        cursor: Long? = null,
        keyword: String? = uiState.value.searchKeyword.trim().takeIf { it.isNotEmpty() },
    ) {
        viewModelScope.launch {
            startLoading()
            resultResponse(
                response = getAdminChallengerListUseCase(cursor, PAGE_SIZE, null, null, keyword, null),
                successCallback = { page ->
                    updateState {
                        copy(
                            challengers = if (cursor == null) page.challengers else challengers + page.challengers,
                            nextCursor = page.nextCursor,
                            hasNext = page.hasNext
                        )
                    }
                },
                errorCallback = { emitEvent(AdminChallengerEvent.ShowToast(it.message)) }
            )
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
    //관리자용 챌린저 목록
    val challengers: List<AdminChallenger> = emptyList(),
    //다음 페이지 커서
    val nextCursor: Long? = null,
    //다음 페이지 존재 여부
    val hasNext: Boolean = false,
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
) : UiState {
    //파트별로 그룹핑된 챌린저 목록
    val sections: List<AdminChallengerSectionUi>
        get() = challengers
            .groupBy { it.part.label }
            .map { (partName, members) ->
                AdminChallengerSectionUi(partName, members.map { it.toMemberUi() })
            }
}

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

//RewardType을 상벌점 부여 요청용 PointType으로 변환
private fun RewardType.toPointType(): PointType {
    return runCatching { PointType.valueOf(name) }.getOrDefault(PointType.CUSTOM)
}
