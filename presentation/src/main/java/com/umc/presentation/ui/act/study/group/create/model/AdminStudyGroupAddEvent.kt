package com.umc.presentation.ui.act.study.group.create.model

import com.umc.presentation.base.UiEvent

sealed interface AdminStudyGroupAddEvent : UiEvent {
    data object ClickBack : AdminStudyGroupAddEvent
    data object ClickRegister : AdminStudyGroupAddEvent
    data object ClickPickLeader : AdminStudyGroupAddEvent
    data object ClickPickMembers : AdminStudyGroupAddEvent
}
