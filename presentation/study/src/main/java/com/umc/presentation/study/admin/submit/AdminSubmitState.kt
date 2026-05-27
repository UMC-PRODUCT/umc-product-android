package com.umc.presentation.study.admin.submit

import com.umc.component.base.UiState

data class AdminSubmitState(
    val selectedWeek: Int = 1,
    val selectedGroupName: String = "전체 그룹",
    val availableWeeks: List<Int> = (1..10).toList(),
    val availableGroups: List<String> = listOf("전체 그룹", "React A팀", "React B팀"),
    val items: List<AdminSubmitItemUiModel> = emptyList(),
) : UiState