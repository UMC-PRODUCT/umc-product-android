package com.umc.presentation.ui.act.study.submit

import com.umc.presentation.base.BaseViewModel


class AdminActStudySubmitViewModel :
    BaseViewModel<AdminActStudySubmitState, AdminActStudySubmitEvent>(
        AdminActStudySubmitState()
    ) {

    fun onAction(action: AdminActStudySubmitAction) {
        when (action) {
            is AdminActStudySubmitAction.SelectWeek -> {
                updateState { copy(selectedWeek = action.week) }

            }

            is AdminActStudySubmitAction.SelectGroupName -> {
                updateState { copy(selectedGroupName = action.name) }

            }

            is AdminActStudySubmitAction.SelectGroup -> {
                updateState { copy(selectedGroupId = action.groupId) }

            }

            is AdminActStudySubmitAction.ClickBest -> {
                updateState { copy(bestDialogTarget = action.item) }
                emitEvent(AdminActStudySubmitEvent.ShowBestDialog(action.item))
            }

            is AdminActStudySubmitAction.ClickReview -> {
                updateState { copy(reviewDialogTarget = action.item) }
                emitEvent(AdminActStudySubmitEvent.ShowReviewDialog(action.item))
            }

            AdminActStudySubmitAction.DismissBestDialog ->
                updateState { copy(bestDialogTarget = null) }

            AdminActStudySubmitAction.DismissReviewDialog ->
                updateState { copy(reviewDialogTarget = null) }

            is AdminActStudySubmitAction.ConfirmBest -> {
                // TODO API
                emitEvent(AdminActStudySubmitEvent.ShowToast("베스트로 설정했어요."))
                updateState { copy(bestDialogTarget = null) }
            }

            is AdminActStudySubmitAction.SubmitReview -> {
                // TODO API
                emitEvent(
                    AdminActStudySubmitEvent.ShowToast(
                        if (action.pass) "통과 처리했어요." else "반려 처리했어요."
                    )
                )
                updateState { copy(reviewDialogTarget = null) }
            }
        }
    }

    fun loadDummy() {
        updateState {
            copy(
                items = listOf(
                    AdminActStudySubmitItemUiModel(
                        userId = 1,
                        name = "홍길동",
                        nickname = "닉네임",
                        partLabel = "iOS",
                        weekText = "1주차",
                        studyTitle = "SwiftUI 플로팅 코딩",
                        submitUrl = "https://github.com/...",
                    ),
                    AdminActStudySubmitItemUiModel(
                        userId = 2,
                        name = "김도연",
                        nickname = "도리",
                        partLabel = "Android",
                        weekText = "1주차",
                        studyTitle = "RecyclerView 구조 잡기",
                        submitUrl = "https://github.com/...",
                        isBest = true
                    ),
                )
            )
        }
    }
}
