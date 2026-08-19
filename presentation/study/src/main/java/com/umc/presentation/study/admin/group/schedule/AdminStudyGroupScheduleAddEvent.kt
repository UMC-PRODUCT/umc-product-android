package com.umc.presentation.study.admin.group.schedule

import com.umc.component.base.UiEvent

/**
 * 관리자 스터디 그룹 일정 화면에서 발생하는 일회성 UI 이벤트
 *
 * 화면 이동이나 토스트처럼 상태로 계속 유지할 필요가 없는
 * 이벤트를 Route에 전달합니다.
 */
sealed interface AdminStudyGroupScheduleEvent : UiEvent {

    // 이전 화면으로 이동
    data object NavigateBack : AdminStudyGroupScheduleEvent

    /**
     * 사용자에게 토스트 메시지 표시
     *
     * 일정 등록 실패 등의 안내 메시지를 전달합니다.
     */
    data class ShowToast(
        val message: String,
    ) : AdminStudyGroupScheduleEvent
}