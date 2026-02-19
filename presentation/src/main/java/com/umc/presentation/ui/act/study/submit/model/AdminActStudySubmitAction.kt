package com.umc.presentation.ui.act.study.submit.model

sealed interface AdminActStudySubmitAction {
    data class SelectWeek(val week: Int) : AdminActStudySubmitAction


    data class SelectGroup(val groupId: Long?) : AdminActStudySubmitAction

    data class SelectGroupName(val name: String) : AdminActStudySubmitAction

    data class ClickBest(val item: AdminActStudySubmitItemUiModel) : AdminActStudySubmitAction
    data class ClickReview(val item: AdminActStudySubmitItemUiModel) : AdminActStudySubmitAction

    object DismissBestDialog : AdminActStudySubmitAction
    object DismissReviewDialog : AdminActStudySubmitAction

    data class ConfirmBest(val reason: String? = null) : AdminActStudySubmitAction
    data class SubmitReview(val pass: Boolean, val url: String, val feedback: String) : AdminActStudySubmitAction
}