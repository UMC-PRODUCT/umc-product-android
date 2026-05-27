package com.umc.presentation.study.admin.submit

import com.umc.component.base.UiEvent

sealed interface AdminSubmitEvent : UiEvent {
    data class ShowToast(val message: String) : AdminSubmitEvent
    data object ShowApproveDialog : AdminSubmitEvent   // 승인 확인 다이얼로그
    data object ShowRejectDialog : AdminSubmitEvent    // 반려 확인 다이얼로그
}