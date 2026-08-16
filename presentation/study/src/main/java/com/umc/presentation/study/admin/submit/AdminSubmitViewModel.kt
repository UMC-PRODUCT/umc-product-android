package com.umc.presentation.study.admin.submit

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.domain.model.base.ApiState
import com.umc.domain.usecase.curriculum.CreateMissionFeedbackUseCase
import com.umc.domain.usecase.curriculum.GetChallengerWorkbookDetailUseCase
import com.umc.domain.usecase.curriculum.GetWorkbookSubmissionWeeksUseCase
import com.umc.domain.usecase.curriculum.GetWorkbookSubmissionsV2UseCase
import com.umc.domain.usecase.curriculum.UpdateMissionFeedbackUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

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

            /*
             * 베스트 관련 API는 필요한 ID가 부족하므로
             * 현재 로컬 UI 동작을 유지
             */
            is AdminSubmitAction.ConfirmBest -> {
                val targetId =
                    uiState.value.bottomSheetItem?.id ?: return
                val comment =
                    uiState.value.bestCommentDraft.trim()

                updateState {
                    copy(
                        items = items.map { item ->
                            if (item.id == targetId) {
                                item.copy(
                                    bestComment = comment,
                                    isBestRegistered = true,
                                )
                            } else {
                                item
                            }
                        },
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
                updateState {
                    copy(
                        items = items.map { item ->
                            if (item.id == bottomSheetItem?.id) {
                                item.copy(
                                    isBestRegistered = false,
                                    bestComment = "",
                                )
                            } else {
                                item
                            }
                        },
                        bottomSheetItem =
                            bottomSheetItem?.copy(
                                isBestRegistered = false,
                                bestComment = "",
                            ),
                        bestCommentDraft = "",
                        isEditingBest = false,
                        showBestCancelDialog = false,
                    )
                }
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
                            availableWeeks = weeks,
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
                                name = member.memberName,
                                nickname = member.nickname,
                                partLabel = member.part,
                                weekText = "${week.weekNo}주차",
                                studyTitle = member.studyGroupName,
                                schoolName = member.schoolName,
                                status = if (week.isBest) {
                                    "BEST"
                                } else {
                                    week.status
                                },
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
                            listOf(
                                AdminSubmitGroupUiModel(
                                    id = null,
                                    name = "전체 그룹",
                                )
                            ) + groups
                        } else {
                            availableGroups
                        }

                        copy(
                            items = if (append) {
                                items + newItems
                            } else {
                                newItems
                            },
                            availableGroups = updatedGroups,
                            isLoading = false,
                            isLoadingMore = false,
                            nextCursor = page.nextCursor,
                            hasNext = page.hasNext,
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

        /*
         * 피드백 수정 API는 내용만 수정 가능
         * 이미 등록된 결과와 다른 PASS/FAIL은 api상 변경이 불가능..
         */
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
                            },
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

    companion object {
        private const val PAGE_SIZE = 20
    }
}

