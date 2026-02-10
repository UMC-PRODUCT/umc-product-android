package com.umc.presentation.ui.act.study.submit.model

import com.umc.presentation.base.UiEvent

sealed interface AdminActStudySubmitEvent : UiEvent {
    data class ShowBestDialog(val item: AdminActStudySubmitItemUiModel) : AdminActStudySubmitEvent
    data class ShowReviewDialog(val item: AdminActStudySubmitItemUiModel) : AdminActStudySubmitEvent
    data class ShowToast(val message: String) : AdminActStudySubmitEvent
}
