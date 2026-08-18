package com.umc.presentation.study.admin.group

import com.umc.component.base.UiState
import com.umc.presentation.study.admin.group.create.AdminStudyGroupCreateMemberUiModel

/**
 * 관리자 스터디 그룹 화면의 UI 상태
 *
 * 스터디 그룹 목록과
 * 그룹 수정 / 삭제 / 멤버 수정에 필요한 상태를 관리합니다.
 *
 * 주요 상태
 * - 스터디 그룹 목록
 * - 목록 로딩 및 페이지네이션
 * - 설정 메뉴 선택 상태
 * - 그룹 정보 수정 Dialog 상태
 * - 그룹 삭제 Dialog 상태
 * - 스터디원 수정 BottomSheet 상태
 */
data class AdminStudyGroupState(

    /**
     * 관리자가 담당하고 있는 스터디 그룹 목록
     */
    val groups: List<AdminStudyGroupItemUiModel> = emptyList(),

    /**
     * 그룹 목록 최초 로딩 여부
     */
    val isLoading: Boolean = false,

    /**
     * 그룹 목록 다음 페이지 로딩 여부
     */
    val isLoadingMore: Boolean = false,

    /**
     * 다음 페이지 조회에 사용할 cursor
     */
    val nextCursor: Long? = null,

    /**
     * 다음 페이지 존재 여부
     */
    val hasNext: Boolean = false,

    /**
     * 현재 설정 Popup이 열려 있는 그룹
     *
     * null이면 어떤 그룹의 설정 메뉴도 열려 있지 않습니다.
     */
    val selectedSettingItem: AdminStudyGroupItemUiModel? = null,

    /**
     * 현재 정보 수정 중인 그룹
     *
     * 값이 존재하면 그룹 정보 수정 Dialog를 표시합니다.
     */
    val editTargetItem: AdminStudyGroupItemUiModel? = null,

    /**
     * 그룹 정보 수정 Dialog에서 입력 중인 그룹 이름
     */
    val editGroupName: String = "",

    /**
     * 그룹 정보 수정 Dialog에서 선택 중인 파트
     */
    val editPartLabel: String = "",

    /**
     * 현재 삭제하려는 그룹
     *
     * 값이 존재하면 그룹 삭제 확인 Dialog를 표시합니다.
     */
    val deleteTargetItem: AdminStudyGroupItemUiModel? = null,

    /**
     * 현재 멤버를 수정 중인 스터디 그룹
     *
     * 값이 존재하면 멤버 수정 BottomSheet를 표시합니다.
     */
    val memberEditTargetItem: AdminStudyGroupItemUiModel? = null,

    /**
     * 멤버 수정 BottomSheet에 표시할
     * 현재 스터디원 목록
     */
    val editingMembers:
    List<AdminStudyGroupCreateMemberUiModel> = emptyList(),
) : UiState {

    /**
     * 그룹 정보 수정 Dialog 표시 여부
     */
    val isEditDialogOpen: Boolean
        get() = editTargetItem != null

    /**
     * 그룹 삭제 확인 Dialog 표시 여부
     */
    val isDeleteDialogOpen: Boolean
        get() = deleteTargetItem != null

    /**
     * 스터디원 수정 BottomSheet 표시 여부
     */
    val isMemberBottomSheetOpen: Boolean
        get() = memberEditTargetItem != null

    /**
     * 그룹 정보 수정 완료 버튼 활성화 여부
     *
     * 현재는 그룹 이름이 비어 있지 않은 경우에만
     * 수정 완료할 수 있습니다.
     */
    val canConfirmEdit: Boolean
        get() = editGroupName.isNotBlank()
}