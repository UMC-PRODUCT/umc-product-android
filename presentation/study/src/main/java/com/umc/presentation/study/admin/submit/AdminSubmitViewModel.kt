package com.umc.presentation.study.admin.submit

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.domain.model.base.ApiState
import com.umc.domain.usecase.curriculum.CreateMissionFeedbackUseCase
import com.umc.domain.usecase.curriculum.GetChallengerWorkbookDetailUseCase
import com.umc.domain.usecase.curriculum.GetWorkbookSubmissionWeeksUseCase
import com.umc.domain.usecase.curriculum.GetWorkbookSubmissionsV2UseCase
import com.umc.domain.usecase.curriculum.UpdateMissionFeedbackUseCase
import com.umc.domain.usecase.curriculum.GetWeeklyBestWorkbooksUseCase
import com.umc.domain.usecase.curriculum.CreateWeeklyBestWorkbookUseCase
import com.umc.domain.usecase.curriculum.UpdateWeeklyBestWorkbookUseCase
import com.umc.domain.usecase.curriculum.DeleteWeeklyBestWorkbookUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 관리자 스터디 제출 현황 화면의 상태와 비즈니스 로직을 관리하는 ViewModel
 *
 * 주요 기능
 * - 제출 가능한 주차 및 그룹별 제출 현황 조회
 * - 커서 기반 제출 목록 페이지네이션
 * - 챌린저 워크북 제출 상세 조회
 * - 제출 피드백 작성 및 수정
 * - PASS / FAIL 승인 및 반려 처리
 * - 베스트 워크북 조회, 등록, 수정 및 취소
 * - 제출 상세 BottomSheet 및 각종 Dialog 상태 관리
 */
@HiltViewModel
class AdminSubmitViewModel @Inject constructor(
    private val getChallengerWorkbookDetailUseCase:
    GetChallengerWorkbookDetailUseCase,
    private val createMissionFeedbackUseCase:
    CreateMissionFeedbackUseCase,
    private val updateMissionFeedbackUseCase:
    UpdateMissionFeedbackUseCase,
    private val getWorkbookSubmissionsV2UseCase:
    GetWorkbookSubmissionsV2UseCase,
    private val getWorkbookSubmissionWeeksUseCase:
    GetWorkbookSubmissionWeeksUseCase,
    private val getWeeklyBestWorkbooksUseCase:
    GetWeeklyBestWorkbooksUseCase,
    private val createWeeklyBestWorkbookUseCase:
    CreateWeeklyBestWorkbookUseCase,
    private val updateWeeklyBestWorkbookUseCase:
    UpdateWeeklyBestWorkbookUseCase,
    private val deleteWeeklyBestWorkbookUseCase:
    DeleteWeeklyBestWorkbookUseCase,
) : BaseViewModel<AdminSubmitState, AdminSubmitEvent>(
    AdminSubmitState()
) {

    init {
        loadWeeks()
        loadSubmissions()
    }

    fun refresh() {
        loadWeeks()
        loadSubmissions()
    }


    fun onAction(action: AdminSubmitAction) {
        when (action) {

            is AdminSubmitAction.OpenBottomSheet -> {
                openBottomSheet(action.item)
            }

            is AdminSubmitAction.CloseBottomSheet -> {
                closeBottomSheet()
            }

            is AdminSubmitAction.OnFeedbackChanged -> {
                updateState {
                    copy(feedback = action.feedback)
                }
            }

            is AdminSubmitAction.OnReviewTabChanged -> {
                updateState {
                    copy(reviewTabIndex = action.index)
                }
            }

            is AdminSubmitAction.ChangeStatus -> {
                updateState {
                    copy(pendingStatus = action.status)
                }
            }

            is AdminSubmitAction.CompleteChange -> {
                val status = uiState.value.pendingStatus ?: return
                submitFeedback(result = status)
            }

            is AdminSubmitAction.ConfirmApprove -> {
                submitFeedback(result = "PASS")
            }

            is AdminSubmitAction.ConfirmReject -> {
                submitFeedback(result = "FAIL")
            }

            is AdminSubmitAction.DismissDialog -> {
                updateState {
                    copy(
                        showApproveDialog = false,
                        showRejectDialog = false,
                    )
                }
            }

            is AdminSubmitAction.SubmitReview -> {
                updateState {
                    if (action.pass) {
                        copy(showApproveDialog = true)
                    } else {
                        copy(showRejectDialog = true)
                    }
                }
            }

            is AdminSubmitAction.OpenWeekBottomSheet -> {
                updateState {
                    copy(showWeekBottomSheet = true)
                }
            }

            is AdminSubmitAction.CloseWeekBottomSheet -> {
                updateState {
                    copy(showWeekBottomSheet = false)
                }
            }

            is AdminSubmitAction.OpenGroupBottomSheet -> {
                updateState {
                    copy(showGroupBottomSheet = true)
                }
            }

            is AdminSubmitAction.CloseGroupBottomSheet -> {
                updateState {
                    copy(showGroupBottomSheet = false)
                }
            }

            is AdminSubmitAction.SelectWeek -> {
                updateState {
                    copy(
                        selectedWeek = action.week,
                        showWeekBottomSheet = false,
                    )
                }

                loadSubmissions(
                    studyGroupId = uiState.value.selectedGroupId,
                    weekNos = listOf(action.week.toLong()),
                )
            }

            is AdminSubmitAction.SelectGroup -> {
                updateState {
                    copy(
                        selectedGroup = action.group,
                        showGroupBottomSheet = false,
                    )
                }

                loadSubmissions(
                    studyGroupId = action.group.id,
                    weekNos = listOf(
                        uiState.value.selectedWeek.toLong()
                    ),
                )

                loadWeeks(
                    studyGroupId = action.group.id,
                )
            }

            is AdminSubmitAction.OnBestCommentChanged -> {
                updateState {
                    copy(bestCommentDraft = action.comment)
                }
            }

            is AdminSubmitAction.ConfirmBest -> {
                val target = uiState.value.bottomSheetItem ?: return

                if (target.isBestRegistered) {
                    updateBestWorkbook()
                } else {
                    registerBestWorkbook()
                }
            }

            is AdminSubmitAction.CancelBest -> {
                updateState {
                    copy(showBestCancelDialog = true)
                }
            }

            is AdminSubmitAction.RegisterBest -> {
                updateState {
                    copy(showBestConfirmDialog = true)
                }
            }

            is AdminSubmitAction.ConfirmCancelBest -> {
                deleteBestWorkbook()
            }

            is AdminSubmitAction.EditBest -> {
                updateState {
                    copy(isEditingBest = true)
                }
            }

            is AdminSubmitAction.CompleteBest -> {
                updateState {
                    copy(showBestConfirmDialog = true)
                }
            }

            is AdminSubmitAction.DismissBestDialog -> {
                updateState {
                    copy(
                        showBestConfirmDialog = false,
                        showBestCancelDialog = false,
                    )
                }
            }

            is AdminSubmitAction.LoadMore -> {
                val state = uiState.value
                val nextCursor = state.nextCursor ?: return

                if (
                    state.isLoading ||
                    state.isLoadingMore ||
                    !state.hasNext
                ) {
                    return
                }

                loadSubmissions(
                    studyGroupId = state.selectedGroupId,
                    weekNos = listOf(state.selectedWeek.toLong()),
                    cursor = nextCursor,
                    append = true,
                )
            }
        }
    }

    private fun openBottomSheet(
        item: AdminSubmitItemUiModel,
    ) {
        updateState {
            copy(
                bottomSheetItem = item,
                feedback = "",
                bestCommentDraft = item.bestComment,
                pendingStatus = null,
                missionSubmissionId = null,
                missionFeedbackId = null,
                existingFeedbackResult = null,
                isEditingBest = false,
            )
        }

        val challengerWorkbookId = item.challengerWorkbookId

        if (challengerWorkbookId == null) {
            emitEvent(
                AdminSubmitEvent.ShowToast(
                    "아직 제출된 워크북이 없어요."
                )
            )
            return
        }

        loadChallengerWorkbookDetail(
            challengerWorkbookId = challengerWorkbookId,
        )
    }

    private fun closeBottomSheet() {
        updateState {
            copy(
                bottomSheetItem = null,
                feedback = "",
                bestCommentDraft = "",
                pendingStatus = null,
                missionSubmissionId = null,
                missionFeedbackId = null,
                existingFeedbackResult = null,
                isEditingBest = false,
                showApproveDialog = false,
                showRejectDialog = false,
            )
        }
    }

    private fun loadChallengerWorkbookDetail(
        challengerWorkbookId: Long,
    ) {
        viewModelScope.launch {
            updateState {
                copy(isLoading = true)
            }

            when (
                val result =
                    getChallengerWorkbookDetailUseCase(
                        challengerWorkbookId =
                            challengerWorkbookId,
                    )
            ) {
                is ApiState.Success -> {
                    val workbook = result.data
                    val submission = workbook.submission
                    val latestFeedback =
                        submission?.feedbacks?.lastOrNull()

                    updateState {
                        copy(
                            isLoading = false,
                            missionSubmissionId =
                                submission?.missionSubmissionId,
                            missionFeedbackId =
                                latestFeedback?.missionFeedbackId,
                            existingFeedbackResult =
                                latestFeedback?.feedbackResult,
                            feedback =
                                latestFeedback?.content.orEmpty(),
                            bottomSheetItem =
                                bottomSheetItem?.copy(
                                    status =
                                        latestFeedback
                                            ?.feedbackResult
                                            ?: bottomSheetItem.status,
                                    isBestRegistered =
                                        workbook.isBestWorkbook,
                                    submitUrl =
                                        submission
                                            ?.submittedContent
                                            .orEmpty(),
                                ),
                        )
                    }
                }

                is ApiState.Fail -> {
                    updateState {
                        copy(isLoading = false)
                    }

                    emitEvent(
                        AdminSubmitEvent.ShowToast(
                            "제출 상세 정보를 불러오지 못했어요."
                        )
                    )
                }
            }
        }
    }


    private fun loadWeeks(
        studyGroupId: Long? = null,
    ) {
        viewModelScope.launch {
            when (
                val result = getWorkbookSubmissionWeeksUseCase(
                    studyGroupId = studyGroupId,
                )
            ) {
                is ApiState.Success -> {
                    val weeks = result.data
                        .map { it.toInt() }
                        .distinct()
                        .sorted()

                    updateState {
                        copy(
                            availableWeeks = weeks.toImmutableList(),
                            selectedWeek = when {
                                weeks.isEmpty() -> 1
                                selectedWeek in weeks -> selectedWeek
                                else -> weeks.first()
                            },
                        )
                    }
                }

                is ApiState.Fail -> {
                    emitEvent(
                        AdminSubmitEvent.ShowToast(
                            "조회 가능한 주차를 불러오지 못했어요."
                        )
                    )
                }
            }
        }
    }

    private fun loadSubmissions(
        studyGroupId: Long? = null,
        weekNos: List<Long>? = null,
        cursor: Long? = null,
        append: Boolean = false,
    ) {
        viewModelScope.launch {
            updateState {
                if (append) {
                    copy(
                        isLoadingMore = true,
                    )
                } else {
                    copy(
                        isLoading = true,
                        nextCursor = null,
                        hasNext = false,
                    )
                }
            }

            when (
                val result = getWorkbookSubmissionsV2UseCase(
                    studyGroupId = studyGroupId,
                    weekNos = weekNos,
                    cursor = cursor,
                    size = PAGE_SIZE,
                )
            ) {
                is ApiState.Success -> {
                    val page = result.data

                    val newItems = page.content.flatMap { member ->
                        member.weeks.map { week ->
                            AdminSubmitItemUiModel(
                                id = member.studyGroupMemberId,
                                challengerWorkbookId = week.challengerWorkbookId,

                                memberId = member.memberId,
                                studyGroupId = member.studyGroupId,
                                weeklyCurriculumId = week.weeklyCurriculumId,

                                name = member.memberName,
                                nickname = member.nickname,
                                partLabel = member.part.label,
                                weekText = "${week.weekNo}주차",
                                studyTitle = member.studyGroupName,
                                profileImageUrl = member.profileImageUrl,
                                schoolName = member.schoolName,
                                status = if (week.isBest) {
                                    "BEST"
                                } else {
                                    week.status
                                },
                                isBestRegistered = week.isBest,
                            )
                        }
                    }

                    val groups = page.content
                        .map { member ->
                            AdminSubmitGroupUiModel(
                                id = member.studyGroupId,
                                name = member.studyGroupName,
                            )
                        }
                        .distinctBy { group ->
                            group.id
                        }

                    updateState {
                        val updatedGroups = if (studyGroupId == null) {
                            (
                                listOf(
                                    AdminSubmitGroupUiModel(
                                        id = null,
                                        name = "전체 그룹",
                                    )
                                ) + groups
                            ).toImmutableList()
                        } else {
                            availableGroups
                        }

                        copy(
                            items = if (append) {
                                (items + newItems).toImmutableList()
                            } else {
                                newItems.toImmutableList()
                            },
                            availableGroups = updatedGroups,
                            isLoading = false,
                            isLoadingMore = false,
                            nextCursor = page.nextCursor,
                            hasNext = page.hasNext,
                        )
                    }

                    if (!append) {
                        loadBestWorkbooks(
                            studyGroupId = studyGroupId,
                            weekNo = weekNos?.singleOrNull(),
                        )
                    }
                }

                is ApiState.Fail -> {
                    updateState {
                        copy(
                            isLoading = false,
                            isLoadingMore = false,
                        )
                    }

                    emitEvent(
                        AdminSubmitEvent.ShowToast(
                            "제출 현황을 불러오지 못했어요."
                        )
                    )
                }
            }
        }
    }

    private fun loadBestWorkbooks(
        studyGroupId: Long? = null,
        weekNo: Long? = null,
    ) {
        viewModelScope.launch {
            when (
                val result = getWeeklyBestWorkbooksUseCase(
                    studyGroupIds = studyGroupId?.let { listOf(it) },
                    weekNos = weekNo?.let { listOf(it) },
                    page = 0,
                    size = 100,
                )
            ) {
                is ApiState.Success -> {
                    val bestWorkbooks = result.data.content

                    updateState {
                        copy(
                            items = items.map { item ->
                                val best = bestWorkbooks.find { best ->
                                    item.challengerWorkbookId != null &&
                                            item.challengerWorkbookId in
                                            best.challengerWorkbookIds
                                }

                                if (best != null) {
                                    item.copy(
                                        weeklyBestWorkbookId =
                                            best.weeklyBestWorkbookId,
                                        bestComment = best.reason,
                                        isBestRegistered = true,
                                    )
                                } else {
                                    item
                                }
                            }.toImmutableList()
                        )
                    }
                }

                is ApiState.Fail -> {
                    emitEvent(
                        AdminSubmitEvent.ShowToast(
                            "베스트 워크북 정보를 불러오지 못했어요."
                        )
                    )
                }
            }
        }
    }

    private fun submitFeedback(
        result: String,
    ) {
        val state = uiState.value

        val targetItem =
            state.bottomSheetItem ?: return

        val missionSubmissionId =
            state.missionSubmissionId

        if (missionSubmissionId == null) {
            emitEvent(
                AdminSubmitEvent.ShowToast(
                    "제출 정보를 찾을 수 없어요."
                )
            )
            return
        }

        val content = state.feedback.trim()

        if (content.isBlank()) {
            emitEvent(
                AdminSubmitEvent.ShowToast(
                    "피드백을 입력해주세요."
                )
            )
            return
        }

        if (
            state.missionFeedbackId != null &&
            state.existingFeedbackResult != null &&
            state.existingFeedbackResult != result
        ) {


        }

        viewModelScope.launch {
            updateState {
                copy(isLoading = true)
            }

            val apiResult =
                if (state.missionFeedbackId == null) {
                    createMissionFeedbackUseCase(
                        missionSubmissionId =
                            missionSubmissionId,
                        content = content,
                        result = result,
                    )
                } else {
                    updateMissionFeedbackUseCase(
                        missionFeedbackId =
                            state.missionFeedbackId,
                        content = content,
                    )
                }

            when (apiResult) {
                is ApiState.Success -> {
                    updateState {
                        copy(
                            isLoading = false,
                            items = items.map { item ->
                                if (item.id == targetItem.id) {
                                    item.copy(status = result)
                                } else {
                                    item
                                }
                            }.toImmutableList(),
                            bottomSheetItem = null,
                            feedback = "",
                            pendingStatus = null,
                            missionSubmissionId = null,
                            missionFeedbackId = null,
                            existingFeedbackResult = null,
                            showApproveDialog = false,
                            showRejectDialog = false,
                        )
                    }

                    emitEvent(
                        AdminSubmitEvent.ShowToast(
                            if (state.missionFeedbackId == null) {
                                if (result == "PASS") {
                                    "통과 처리됐어요."
                                } else {
                                    "반려 처리됐어요."
                                }
                            } else {
                                "피드백이 수정됐어요."
                            }
                        )
                    )
                }

                is ApiState.Fail -> {
                    updateState {
                        copy(
                            isLoading = false,
                            showApproveDialog = false,
                            showRejectDialog = false,
                        )
                    }

                    emitEvent(
                        AdminSubmitEvent.ShowToast(
                            "피드백 저장에 실패했어요."
                        )
                    )
                }
            }
        }
    }

    private fun registerBestWorkbook() {
        val state = uiState.value
        val target = state.bottomSheetItem ?: return
        val comment = state.bestCommentDraft.trim()

        if (comment.isBlank()) {
            emitEvent(
                AdminSubmitEvent.ShowToast(
                    "베스트 선정 사유를 입력해주세요."
                )
            )
            return
        }

        viewModelScope.launch {
            when (
                createWeeklyBestWorkbookUseCase(
                    bestMemberId = target.memberId,
                    weeklyCurriculumId = target.weeklyCurriculumId,
                    studyGroupId = target.studyGroupId,
                    reason = comment,
                )
            ) {
                is ApiState.Success -> {
                    updateState {
                        copy(
                            items = items.map { item ->
                                if (
                                    item.id == target.id &&
                                    item.weeklyCurriculumId ==
                                    target.weeklyCurriculumId
                                ) {
                                    item.copy(
                                        bestComment = comment,
                                        isBestRegistered = true,
                                    )
                                } else {
                                    item
                                }
                            }.toImmutableList(),
                            bottomSheetItem =
                                bottomSheetItem?.copy(
                                    bestComment = comment,
                                    isBestRegistered = true,
                                ),
                            bestCommentDraft = comment,
                            isEditingBest = false,
                            showBestConfirmDialog = false,
                        )
                    }

                    emitEvent(
                        AdminSubmitEvent.ShowToast(
                            "베스트 워크북으로 선정했어요."
                        )
                    )

                    loadBestWorkbooks(
                        studyGroupId = target.studyGroupId,
                    )
                }

                is ApiState.Fail -> {
                    updateState {
                        copy(
                            showBestConfirmDialog = false,
                        )
                    }

                    emitEvent(
                        AdminSubmitEvent.ShowToast(
                            "베스트 워크북 선정에 실패했어요."
                        )
                    )
                }
            }
        }
    }

    private fun updateBestWorkbook() {
        val state = uiState.value
        val target = state.bottomSheetItem ?: return

        val weeklyBestWorkbookId =
            target.weeklyBestWorkbookId ?: return

        val comment = state.bestCommentDraft.trim()

        if (comment.isBlank()) {
            emitEvent(
                AdminSubmitEvent.ShowToast(
                    "베스트 선정 사유를 입력해주세요."
                )
            )
            return
        }

        viewModelScope.launch {
            when (
                updateWeeklyBestWorkbookUseCase(
                    weeklyBestWorkbookId = weeklyBestWorkbookId,
                    reason = comment,
                )
            ) {
                is ApiState.Success -> {
                    updateState {
                        copy(
                            items = items.map { item ->
                                if (
                                    item.weeklyBestWorkbookId ==
                                    weeklyBestWorkbookId
                                ) {
                                    item.copy(
                                        bestComment = comment,
                                    )
                                } else {
                                    item
                                }
                            }.toImmutableList(),
                            bottomSheetItem =
                                bottomSheetItem?.copy(
                                    bestComment = comment,
                                ),
                            bestCommentDraft = comment,
                            isEditingBest = false,
                            showBestConfirmDialog = false,
                        )
                    }

                    emitEvent(
                        AdminSubmitEvent.ShowToast(
                            "베스트 선정 사유가 수정됐어요."
                        )
                    )
                }

                is ApiState.Fail -> {
                    updateState {
                        copy(
                            showBestConfirmDialog = false,
                        )
                    }

                    emitEvent(
                        AdminSubmitEvent.ShowToast(
                            "베스트 선정 사유 수정에 실패했어요."
                        )
                    )
                }
            }
        }
    }

    private fun deleteBestWorkbook() {
        val target =
            uiState.value.bottomSheetItem ?: return

        val weeklyBestWorkbookId =
            target.weeklyBestWorkbookId ?: return

        viewModelScope.launch {
            when (
                deleteWeeklyBestWorkbookUseCase(
                    weeklyBestWorkbookId =
                        weeklyBestWorkbookId,
                )
            ) {
                is ApiState.Success -> {
                    updateState {
                        copy(
                            items = items.map { item ->
                                if (
                                    item.weeklyBestWorkbookId ==
                                    weeklyBestWorkbookId
                                ) {
                                    item.copy(
                                        weeklyBestWorkbookId = null,
                                        isBestRegistered = false,
                                        bestComment = "",
                                        status = if (item.status == "BEST") {
                                            "PASS"
                                        } else {
                                            item.status
                                        },
                                    )
                                } else {
                                    item
                                }
                            }.toImmutableList(),
                            bottomSheetItem =
                                bottomSheetItem?.copy(
                                    weeklyBestWorkbookId = null,
                                    isBestRegistered = false,
                                    bestComment = "",
                                    status = if (
                                        bottomSheetItem?.status == "BEST"
                                    ) {
                                        "PASS"
                                    } else {
                                        bottomSheetItem?.status
                                            ?: ""
                                    },
                                ),
                            bestCommentDraft = "",
                            isEditingBest = false,
                            showBestCancelDialog = false,
                        )
                    }

                    emitEvent(
                        AdminSubmitEvent.ShowToast(
                            "베스트 선정을 취소했어요."
                        )
                    )
                }

                is ApiState.Fail -> {
                    updateState {
                        copy(
                            showBestCancelDialog = false,
                        )
                    }

                    emitEvent(
                        AdminSubmitEvent.ShowToast(
                            "베스트 선정 취소에 실패했어요."
                        )
                    )
                }
            }
        }
    }

    companion object {
        private const val PAGE_SIZE = 20
    }
}

