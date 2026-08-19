package com.umc.presentation.study.admin.group

import com.umc.component.base.UiEvent

/**
 * 관리자 스터디 그룹 화면의 일회성 UI 이벤트
 *
 * ViewModel에서 Route로
 * 네비게이션이나 Toast 출력 요청을 전달하기 위해 사용합니다.
 */
sealed interface AdminStudyGroupEvent : UiEvent {

    /**
     * 스터디 그룹 생성 화면으로 이동
     */
    data object NavigateCreateGroup : AdminStudyGroupEvent

    /**
     * 선택한 스터디 그룹의 일정 등록 화면으로 이동
     *
     * @param groupId 스터디 그룹 ID
     * @param groupTitle 스터디 그룹 이름
     * @param groupPart 스터디 그룹 파트
     */
    data class NavigateAddSchedule(
        val groupId: Long,
        val groupTitle: String,
        val groupPart: String,
    ) : AdminStudyGroupEvent

    /**
     * 스터디원 수정 화면 열기
     *
     * @param item 수정할 스터디 그룹 정보
     */
    data class OpenEditMembers(
        val item: AdminStudyGroupItemUiModel,
    ) : AdminStudyGroupEvent

    /**
     * 사용자에게 Toast 메시지 표시
     *
     * @param message 표시할 메시지
     */
    data class ShowToast(
        val message: String,
    ) : AdminStudyGroupEvent
}