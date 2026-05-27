package com.umc.presentation.study.admin.submit

import com.umc.component.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AdminSubmitViewModel @Inject constructor() :
    BaseViewModel<AdminSubmitState, AdminSubmitEvent>(AdminSubmitState()) {

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

            // 바텀시트 열기/닫기
            is AdminSubmitAction.OpenBottomSheet ->
                updateState { copy(bottomSheetItem = action.item, feedback = "", pendingStatus = null) }
            is AdminSubmitAction.CloseBottomSheet ->
                updateState { copy(bottomSheetItem = null, feedback = "", pendingStatus = null) }

            // 피드백 입력
            is AdminSubmitAction.OnFeedbackChanged ->
                updateState { copy(feedback = action.feedback) }

            // 탭 전환
            is AdminSubmitAction.OnReviewTabChanged ->
                updateState { copy(reviewTabIndex = action.index) }


            // 현황 변경 (Pass/Fail 선택)
            is AdminSubmitAction.ChangeStatus ->
                updateState { copy(pendingStatus = action.status) }

            // 완료하기
            is AdminSubmitAction.CompleteChange -> {
                val status = uiState.value.pendingStatus ?: return
                updateState {
                    copy(
                        items = items.map {
                            if (it.id == bottomSheetItem?.id) it.copy(status = status) else it
                        },
                        bottomSheetItem = null,
                        feedback = "",
                        pendingStatus = null
                    )
                }
                emitEvent(AdminSubmitEvent.ShowToast("변경 완료!"))
            }

            is AdminSubmitAction.ConfirmApprove -> {
                updateState {
                    copy(
                        items = items.map {
                            if (it.id == bottomSheetItem?.id) it.copy(status = "PASS") else it
                        },
                        bottomSheetItem = null,
                        feedback = "",
                        showApproveDialog = false
                    )
                }
                emitEvent(AdminSubmitEvent.ShowToast("통과 처리됐어요."))
            }
            is AdminSubmitAction.ConfirmReject -> {
                updateState {
                    copy(
                        items = items.map {
                            if (it.id == bottomSheetItem?.id) it.copy(status = "FAIL") else it
                        },
                        bottomSheetItem = null,
                        feedback = "",
                        showRejectDialog = false
                    )
                }
                emitEvent(AdminSubmitEvent.ShowToast("반려 처리됐어요."))
            }

            //바텀시트
            is AdminSubmitAction.DismissDialog -> {
                updateState { copy(showApproveDialog = false, showRejectDialog = false) }
            }
            is AdminSubmitAction.SubmitReview -> {
                if (action.pass) updateState { copy(showApproveDialog = true) }
                else updateState { copy(showRejectDialog = true) }
            }

            is AdminSubmitAction.OpenWeekBottomSheet ->
                updateState { copy(showWeekBottomSheet = true) }

            is AdminSubmitAction.CloseWeekBottomSheet ->
                updateState { copy(showWeekBottomSheet = false) }

            is AdminSubmitAction.OpenGroupBottomSheet ->
                updateState { copy(showGroupBottomSheet = true) }

            is AdminSubmitAction.CloseGroupBottomSheet ->
                updateState { copy(showGroupBottomSheet = false) }

            is AdminSubmitAction.SelectWeek -> {
                updateState { copy(selectedWeek = action.week, showWeekBottomSheet = false) }
            }
            is AdminSubmitAction.SelectGroup -> {
                updateState { copy(selectedGroupName = action.name, showGroupBottomSheet = false) }
            }

            is AdminSubmitAction.OnBestCommentChanged ->
                updateState {
                    copy(
                        items = items.map {
                            if (it.id == bottomSheetItem?.id) it.copy(bestComment = action.comment) else it
                        },
                        bottomSheetItem = bottomSheetItem?.copy(bestComment = action.comment)
                    )
                }

            is AdminSubmitAction.ConfirmBest ->
                updateState {
                    copy(
                        items = items.map {
                            if (it.id == bottomSheetItem?.id) it.copy(isBestRegistered = true) else it
                        },
                        bottomSheetItem = bottomSheetItem?.copy(isBestRegistered = true),
                        isEditingBest = false,
                        showBestConfirmDialog = false
                    )
                }

            is AdminSubmitAction.CancelBest ->
                updateState { copy(showBestCancelDialog = true) }

            is AdminSubmitAction.RegisterBest ->
                updateState { copy(showBestConfirmDialog = true) }

            is AdminSubmitAction.ConfirmCancelBest ->
                updateState {
                    copy(
                        items = items.map {
                            if (it.id == bottomSheetItem?.id) it.copy(
                                isBestRegistered = false,
                                bestComment = ""
                            ) else it
                        },
                        bottomSheetItem = bottomSheetItem?.copy(isBestRegistered = false, bestComment = ""),
                        isEditingBest = false,
                        showBestCancelDialog = false
                    )
                }

            is AdminSubmitAction.EditBest ->
                updateState { copy(isEditingBest = true) }

            is AdminSubmitAction.CompleteBest ->
                updateState { copy(showBestConfirmDialog = true) }

            is AdminSubmitAction.DismissBestDialog ->
                updateState { copy(showBestConfirmDialog = false, showBestCancelDialog = false) }
        }
    }
}