package com.umc.presentation.study.admin.group

import com.umc.presentation.study.admin.group.create.AdminStudyGroupCreateMemberUiModel

/**
 * 관리자 스터디 그룹 화면에서 발생하는 사용자 액션
 *
 * 화면에서 발생한 클릭, 입력, 다이얼로그 및 바텀시트 동작을
 * ViewModel에 전달하기 위해 사용합니다.
 *
 * 주요 기능
 * - 그룹 목록 조회
 * - 그룹 생성 화면 이동
 * - 그룹 설정 메뉴 열기/닫기
 * - 일정 등록 화면 이동
 * - 그룹 정보 수정
 * - 그룹 삭제
 * - 스터디원 수정
 */
sealed interface AdminStudyGroupAction {

    /**
     * 스터디 그룹 목록 조회
     */
    data object LoadGroups : AdminStudyGroupAction

    /**
     * 스터디 그룹 생성 버튼 클릭
     *
     * 그룹 생성 화면으로 이동합니다.
     */
    data object ClickCreateGroup : AdminStudyGroupAction

    /**
     * 그룹 카드의 설정 버튼 클릭
     *
     * @param item 설정 메뉴를 표시할 그룹
     */
    data class ClickSetting(
        val item: AdminStudyGroupItemUiModel,
    ) : AdminStudyGroupAction

    /**
     * 그룹 설정 Popup 닫기
     */
    data object DismissSettingPopup : AdminStudyGroupAction

    /**
     * 스터디 일정 등록 버튼 클릭
     *
     * @param item 일정을 등록할 스터디 그룹
     */
    data class ClickAddSchedule(
        val item: AdminStudyGroupItemUiModel,
    ) : AdminStudyGroupAction

    /**
     * 스터디원 수정 요청
     *
     * @param item 멤버를 수정할 스터디 그룹
     */
    data class ClickEditMembers(
        val item: AdminStudyGroupItemUiModel,
    ) : AdminStudyGroupAction

    /**
     * 그룹 정보 수정 Dialog 열기
     *
     * @param item 수정할 스터디 그룹
     */
    data class OpenEditDialog(
        val item: AdminStudyGroupItemUiModel,
    ) : AdminStudyGroupAction

    /**
     * 그룹 정보 수정 Dialog 닫기
     */
    data object CloseEditDialog : AdminStudyGroupAction

    /**
     * 그룹 정보 수정 중 그룹 이름 변경
     *
     * @param name 새로 입력한 그룹 이름
     */
    data class OnEditGroupNameChanged(
        val name: String,
    ) : AdminStudyGroupAction

    /**
     * 그룹 정보 수정 중 파트 변경
     *
     * @param partLabel 새로 선택한 파트
     */
    data class OnEditPartChanged(
        val partLabel: String,
    ) : AdminStudyGroupAction

    /**
     * 그룹 정보 수정 완료
     */
    data object ConfirmEditGroup : AdminStudyGroupAction

    /**
     * 그룹 삭제 확인 Dialog 열기
     *
     * @param item 삭제할 스터디 그룹
     */
    data class OpenDeleteDialog(
        val item: AdminStudyGroupItemUiModel,
    ) : AdminStudyGroupAction

    /**
     * 그룹 삭제 확인 Dialog 닫기
     */
    data object CloseDeleteDialog : AdminStudyGroupAction

    /**
     * 그룹 삭제 확정
     */
    data object ConfirmDeleteGroup : AdminStudyGroupAction

    /**
     * 그룹 카드의 스터디원 추가 버튼 클릭
     *
     * 멤버 수정 BottomSheet를 표시합니다.
     *
     * @param item 멤버를 수정할 스터디 그룹
     */
    data class OpenMemberBottomSheet(
        val item: AdminStudyGroupItemUiModel,
    ) : AdminStudyGroupAction

    /**
     * 멤버 수정 BottomSheet 닫기
     */
    data object CloseMemberBottomSheet : AdminStudyGroupAction

    /**
     * 멤버 수정 BottomSheet에서 확정된
     * 최종 멤버 목록을 ViewModel에 전달합니다.
     *
     * @param members 수정 완료된 멤버 목록
     */
    data class ConfirmMemberChanges(
        val members: List<AdminStudyGroupCreateMemberUiModel>,
    ) : AdminStudyGroupAction
}