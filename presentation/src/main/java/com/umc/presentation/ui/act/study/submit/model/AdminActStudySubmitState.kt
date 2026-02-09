package com.umc.presentation.ui.act.study.submit.model

import com.umc.presentation.base.UiState

data class AdminActStudySubmitState(
    val selectedWeek: Int = 1,

    val selectedGroupName: String = "전체 그룹",

    val groupOptions: List<String> = listOf(
        "전체 그룹", "React A팀", "React B팀", "Spring A팀", "Android A팀", "iOS A팀"
    ),

    val selectedGroupId: Long? = null,

    val items: List<AdminActStudySubmitItemUiModel> = emptyList(),

    val bestDialogTarget: AdminActStudySubmitItemUiModel? = null,
    val reviewDialogTarget: AdminActStudySubmitItemUiModel? = null,
) : UiState
