package com.umc.presentation.study.admin.group.create

import com.umc.component.base.UiEvent

/**
 * 스터디 그룹 생성 과정에서 발생하는 일회성 UI 이벤트
 *
 * ViewModel에서 그룹 생성 결과나 화면 이동 요청을
 * UI(Route)에 전달하기 위해 사용합니다.
 *
 * 상태로 계속 유지할 필요가 없는
 * 네비게이션, 성공/실패 결과 등을 처리합니다.
 */
sealed interface AdminStudyGroupCreateEvent : UiEvent {

    /**
     * 이전 화면으로 이동
     */
    data object NavigateBack : AdminStudyGroupCreateEvent

    /**
     * 스터디 그룹 생성 성공
     *
     * Route에서 이벤트를 수신한 뒤
     * 이전 화면으로 이동합니다.
     */
    data object RegisterSuccess : AdminStudyGroupCreateEvent

    /**
     * 스터디 그룹 생성 실패
     *
     * @param message 서버 또는 API에서 전달된 실패 메시지
     */
    data class RegisterFailure(
        val message: String,
    ) : AdminStudyGroupCreateEvent
}