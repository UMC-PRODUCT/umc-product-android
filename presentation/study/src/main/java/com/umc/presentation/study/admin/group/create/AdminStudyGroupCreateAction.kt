package com.umc.presentation.study.admin.group.create

/**
 * 스터디 그룹 생성 화면에서 발생하는 사용자 액션
 *
 * 화면에서 발생한 클릭이나 입력 이벤트를
 * ViewModel에 전달하기 위해 사용합니다.
 *
 * 주요 액션
 * - 그룹 이름 입력
 * - 파트 선택
 * - 담당 파트장 선택
 * - 스터디원 선택
 * - 그룹 등록
 * - 뒤로가기
 */
sealed interface AdminStudyGroupCreateAction {

    /**
     * 그룹 이름 입력값 변경
     *
     * @param value 사용자가 입력한 그룹 이름
     */
    data class OnGroupNameChanged(
        val value: String,
    ) : AdminStudyGroupCreateAction

    /**
     * 파트 선택 영역 클릭
     *
     * 파트 선택 바텀시트를 표시합니다.
     */
    data object OnPartClick : AdminStudyGroupCreateAction

    /**
     * 담당 파트장 선택 영역 클릭
     *
     * 파트장 검색 및 선택 바텀시트를 표시합니다.
     */
    data object OnPartLeaderClick : AdminStudyGroupCreateAction

    /**
     * 스터디원 선택 영역 클릭
     *
     * 스터디원 검색 및 선택 바텀시트를 표시합니다.
     */
    data object OnMemberClick : AdminStudyGroupCreateAction

    /**
     * 그룹 등록 버튼 클릭
     *
     * 현재 입력된 정보를 기반으로
     * 스터디 그룹 생성을 요청합니다.
     */
    data object OnRegisterClick : AdminStudyGroupCreateAction

    /**
     * 뒤로가기 버튼 클릭
     *
     * 현재 그룹 생성 화면을 종료합니다.
     */
    data object OnBackClick : AdminStudyGroupCreateAction
}