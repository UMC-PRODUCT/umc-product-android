package com.umc.presentation.study.admin.group.schedule

import com.umc.component.base.UiEvent
import com.umc.component.component.UToastState

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
     * 일정 등록 성공(CHECK)과 조회·등록 실패(ERROR) 안내를 전달합니다.
     * 성공 토스트 뒤에는 곧바로 [NavigateBack] 이 오므로 화면을 떠나도 남는 앱 토스트로 띄운다.
     */
    data class ShowToast(
        val message: String,
        val state: UToastState,
    ) : AdminStudyGroupScheduleEvent
}