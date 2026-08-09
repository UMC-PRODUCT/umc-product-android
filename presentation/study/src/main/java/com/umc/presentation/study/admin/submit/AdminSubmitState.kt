package com.umc.presentation.study.admin.submit

import com.umc.component.base.UiState

data class AdminSubmitGroupUiModel(
    val id: Long?,
    val name: String,
)

data class AdminSubmitState(
    val selectedWeek: Int = 1,

    val selectedGroup: AdminSubmitGroupUiModel =
        AdminSubmitGroupUiModel(
            id = null,
            name = "전체 그룹",
        ),

    // API에서 조회한 제출 현황 주차 목록
    val availableWeeks: List<Int> = emptyList(),

    // TODO 그룹 조회 API 연결 후 교체
    val availableGroups: List<AdminSubmitGroupUiModel> =
        listOf(
            AdminSubmitGroupUiModel(
                id = null,
                name = "전체 그룹",
            )
        ),

    // 제출 현황 목록
    val items: List<AdminSubmitItemUiModel> = emptyList(),

    val isLoading: Boolean = false,

    // 제출 현황 커서 페이지네이션
    val nextCursor: Long? = null,
    val hasNext: Boolean = false,
    val isLoadingMore: Boolean = false,

    // 바텀시트
    val bottomSheetItem: AdminSubmitItemUiModel? = null,
    val feedback: String = "",
    val bestCommentDraft: String = "",
    val reviewTabIndex: Int = 0,
    val pendingStatus: String? = null,

    // 상세 조회로 받아오는 서버 ID
    val missionSubmissionId: Long? = null,
    val missionFeedbackId: Long? = null,
    val existingFeedbackResult: String? = null,

    // 다이얼로그 상태
    val showApproveDialog: Boolean = false,
    val showRejectDialog: Boolean = false,

    // 워크북 제출
    val showWeekBottomSheet: Boolean = false,
    val showGroupBottomSheet: Boolean = false,

    // 베스트 워크북
    val isEditingBest: Boolean = false,
    val showBestConfirmDialog: Boolean = false,
    val showBestCancelDialog: Boolean = false,
) : UiState {

    val selectedGroupName: String
        get() = selectedGroup.name

    val selectedGroupId: Long?
        get() = selectedGroup.id

    val isBottomSheetOpen: Boolean
        get() = bottomSheetItem != null

    val isSubmitEnabled: Boolean
        get() = feedback.isNotBlank()

    val isBestSubmitEnabled: Boolean
        get() = bestCommentDraft.isNotBlank()

    val isReviewed: Boolean
        get() = existingFeedbackResult != null ||
                bottomSheetItem?.markStatus != null

    val isBestRegistered: Boolean
        get() = bottomSheetItem?.isBestRegistered ?: false
}