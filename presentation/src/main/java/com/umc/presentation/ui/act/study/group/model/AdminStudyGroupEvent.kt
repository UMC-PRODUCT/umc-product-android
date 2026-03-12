package com.umc.presentation.ui.act.study.group.model

import com.umc.presentation.base.UiEvent

sealed interface AdminStudyGroupEvent : UiEvent {
    data object ClickCreateGroup : AdminStudyGroupEvent
    data class ClickEditMembers(val groupId: Long) : AdminStudyGroupEvent
    data class ShowToast(val message: String) : AdminStudyGroupEvent
}