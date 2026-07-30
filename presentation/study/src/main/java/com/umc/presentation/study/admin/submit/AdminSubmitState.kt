package com.umc.presentation.study.admin.submit

import com.umc.component.base.UiState

data class AdminSubmitState(
    val selectedWeek: Int = 1,
    val selectedGroupName: String = "전체 그룹",
    val availableWeeks: List<Int> = (1..10).toList(),
    val availableGroups: List<String> =
        listOf("전체 그룹", "React A팀", "React B팀"),
    val items: List<AdminSubmitItemUiModel> = emptyList(),

    val isLoading: Boolean = false,

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