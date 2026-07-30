package com.umc.presentation.study.admin.submit

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.domain.model.base.ApiState
import com.umc.domain.usecase.curriculum.CreateMissionFeedbackUseCase
import com.umc.domain.usecase.curriculum.GetChallengerWorkbookDetailUseCase
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
) : BaseViewModel<AdminSubmitState, AdminSubmitEvent>(
    AdminSubmitState()
) {

    init {
        loadDummy()
    }

    private fun loadDummy() {
        updateState {
            copy(
                items = listOf(
                    AdminSubmitItemUiModel(
                        id = 1L,
                        name = "홍길동",
                        nickname = "닉네임",
                        partLabel = "iOS",
                        weekText = "1주차",
                        studyTitle = "SwiftUI 클론 코딩",
                        schoolName = "중앙대",
                        status = "BEST",
                    ),
                    AdminSubmitItemUiModel(
                        id = 2L,
                        name = "홍길동",
                        nickname = "닉네임",
                        partLabel = "iOS",
                        weekText = "1주차",
                        studyTitle = "SwiftUI 클론 코딩",
                        schoolName = "중앙대",
                        status = "FAIL",
                    ),
                    AdminSubmitItemUiModel(
                        id = 3L,
                        name = "홍길동",
                        nickname = "닉네임",
                        partLabel = "iOS",
                        weekText = "1주차",
                        studyTitle = "SwiftUI 클론 코딩",
                        schoolName = "중앙대",
                        status = "PASS",
                    ),
                    AdminSubmitItemUiModel(
                        id = 4L,
                        name = "홍길동",
                        nickname = "닉네임",
                        partLabel = "iOS",
                        weekText = "1주차",
                        studyTitle = "SwiftUI 클론 코딩",
                        schoolName = "중앙대",
                        status = "SUBMITTED",
                    ),
                    AdminSubmitItemUiModel(
                        id = 5L,
                        name = "홍길동",
                        nickname = "닉네임",
                        partLabel = "iOS",
                        weekText = "1주차",
                        studyTitle = "SwiftUI 클론 코딩",
                        schoolName = "중앙대",
                        status = "SUBMITTED",
                    ),
                )
            )
        }
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
            }

            is AdminSubmitAction.SelectGroup -> {
                updateState {
                    copy(
                        selectedGroupName = action.name,
                        showGroupBottomSheet = false,
                    )
                }
            }

            is AdminSubmitAction.OnBestCommentChanged -> {
                updateState {
                    copy(bestCommentDraft = action.comment)
                }
            }

            /*
             * 베스트 관련 API는 필요한 ID가 부족하므로
             * 현재 로컬 UI 동작을 유지한다.
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

        loadChallengerWorkbookDetail(
            challengerWorkbookId = item.id,
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
}