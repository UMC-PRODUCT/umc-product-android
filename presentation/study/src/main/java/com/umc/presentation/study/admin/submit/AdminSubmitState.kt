package com.umc.presentation.study.admin.submit

import com.umc.component.base.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * 관리자 제출 현황의 그룹 필터 UI 모델
 */
data class AdminSubmitGroupUiModel(
    val id: Long?,
    val name: String,
)

/**
 * 관리자 스터디 제출 현황 화면에서 사용하는 UI 상태
 *
 * 주차/그룹 필터, 제출 목록, 상세 BottomSheet,
 * 승인/반려 및 베스트 워크북 관련 상태를 관리합니다.
 */
data class AdminSubmitState(

    /** 현재 선택된 주차 */
    val selectedWeek: Int = 1,

    /** 현재 선택된 스터디 그룹 */
    val selectedGroup: AdminSubmitGroupUiModel =
        AdminSubmitGroupUiModel(
            id = null,
            name = "전체 그룹",
        ),

    /** API에서 조회한 제출 현황 주차 목록 */
    val availableWeeks: ImmutableList<Int> = persistentListOf(),

    /** 선택 가능한 스터디 그룹 목록 */
    val availableGroups: ImmutableList<AdminSubmitGroupUiModel> =
        persistentListOf(
            AdminSubmitGroupUiModel(
                id = null,
                name = "전체 그룹",
            )
        ),

    /** 현재 조회된 제출 현황 목록 */
    val items: ImmutableList<AdminSubmitItemUiModel> = persistentListOf(),

    /** 제출 현황 또는 상세 정보 로딩 여부 */
    val isLoading: Boolean = false,

    /** 다음 페이지 조회용 cursor */
    val nextCursor: Long? = null,

    /** 다음 페이지 존재 여부 */
    val hasNext: Boolean = false,

    /** 다음 페이지 로딩 여부 */
    val isLoadingMore: Boolean = false,

    /** 상세 BottomSheet에 표시할 제출 항목 */
    val bottomSheetItem: AdminSubmitItemUiModel? = null,

    /** 관리자 피드백 입력값 */
    val feedback: String = "",

    /** 베스트 선정 사유 입력값 */
    val bestCommentDraft: String = "",

    /** 제출 상세 BottomSheet의 현재 탭 */
    val reviewTabIndex: Int = 0,

    /** 변경 예정인 PASS / FAIL 상태 */
    val pendingStatus: String? = null,

    /** 제출 상세 조회로 받아온 제출 ID */
    val missionSubmissionId: Long? = null,

    /** 기존 피드백 ID */
    val missionFeedbackId: Long? = null,

    /** 서버에 저장된 기존 피드백 결과 */
    val existingFeedbackResult: String? = null,

    /** 승인 확인 Dialog 표시 여부 */
    val showApproveDialog: Boolean = false,

    /** 반려 확인 Dialog 표시 여부 */
    val showRejectDialog: Boolean = false,

    /** 주차 선택 BottomSheet 표시 여부 */
    val showWeekBottomSheet: Boolean = false,

    /** 그룹 선택 BottomSheet 표시 여부 */
    val showGroupBottomSheet: Boolean = false,

    /** 베스트 선정 사유 수정 중인지 여부 */
    val isEditingBest: Boolean = false,

    /** 베스트 등록/수정 확인 Dialog 표시 여부 */
    val showBestConfirmDialog: Boolean = false,

    /** 베스트 선정 취소 Dialog 표시 여부 */
    val showBestCancelDialog: Boolean = false,
) : UiState {

    /** 현재 선택된 그룹 이름 */
    val selectedGroupName: String
        get() = selectedGroup.name

    /** 현재 선택된 그룹 ID */
    val selectedGroupId: Long?
        get() = selectedGroup.id

    /** 제출 상세 BottomSheet 표시 여부 */
    val isBottomSheetOpen: Boolean
        get() = bottomSheetItem != null

    /** 피드백 저장 버튼 활성화 여부 */
    val isSubmitEnabled: Boolean
        get() = feedback.isNotBlank()

    /** 베스트 선정 저장 버튼 활성화 여부 */
    val isBestSubmitEnabled: Boolean
        get() = bestCommentDraft.isNotBlank()

    /** 해당 제출이 이미 평가된 상태인지 여부 */
    val isReviewed: Boolean
        get() = existingFeedbackResult != null ||
                bottomSheetItem?.markStatus != null

    /** 현재 항목이 베스트 워크북으로 등록된 상태인지 여부 */
    val isBestRegistered: Boolean
        get() = bottomSheetItem?.isBestRegistered ?: false
}