package com.umc.presentation.study.admin.submit

import com.umc.component.base.UiState

data class AdminSubmitState(
    val selectedWeek: Int = 1,
    val selectedGroupName: String = "전체 그룹",
    val availableWeeks: List<Int> = (1..10).toList(),
    val availableGroups: List<String> = listOf("전체 그룹", "React A팀", "React B팀"),
    val items: List<AdminSubmitItemUiModel> = emptyList(),

    // 바텀시트
    val bottomSheetItem: AdminSubmitItemUiModel? = null,  // null이면 닫힘
    val feedback: String = "",
    val reviewTabIndex: Int = 0,          // 0: 검토, 1: 베스트 워크북
    val pendingStatus: String? = null,    // 현황 변경 시 선택한 상태

    // 다이얼로그 상태
    val showApproveDialog: Boolean = false,
    val showRejectDialog: Boolean = false,

    val showWeekBottomSheet: Boolean = false,
    val showGroupBottomSheet: Boolean = false,
) : UiState {
    val isBottomSheetOpen: Boolean get() = bottomSheetItem != null
    val isSubmitEnabled: Boolean get() = feedback.isNotBlank()
    val isReviewed: Boolean get() = bottomSheetItem?.markStatus != null
}