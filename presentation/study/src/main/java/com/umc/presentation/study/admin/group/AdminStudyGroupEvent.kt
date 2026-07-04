package com.umc.presentation.study.admin.group

import com.umc.component.base.UiEvent

sealed interface AdminStudyGroupEvent : UiEvent {
    data object NavigateCreateGroup : AdminStudyGroupEvent

    data class NavigateAddSchedule(
        val groupId: Long,
        val groupTitle: String,
        val groupPart: String,
    ) : AdminStudyGroupEvent

    data class OpenEditMembers(
        val item: AdminStudyGroupItemUiModel,
    ) : AdminStudyGroupEvent

    data class ShowToast(
        val message: String,
    ) : AdminStudyGroupEvent
}