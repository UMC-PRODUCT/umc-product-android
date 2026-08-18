package com.umc.presentation.study.admin.submit

import com.umc.component.base.UiEvent

/**
 * 관리자 제출 현황 화면에서 발생하는 일회성 UI 이벤트
 */
sealed interface AdminSubmitEvent : UiEvent {

    /** 사용자에게 Toast 메시지 표시 */
    data class ShowToast(
        val message: String,
    ) : AdminSubmitEvent
}