package com.umc.presentation.study.admin.group.create

sealed interface AdminStudyGroupCreateAction {
    data class OnGroupNameChanged(val value: String) : AdminStudyGroupCreateAction
    data object OnPartClick : AdminStudyGroupCreateAction
    data object OnPartLeaderClick : AdminStudyGroupCreateAction
    data object OnMemberClick : AdminStudyGroupCreateAction
    data object OnRegisterClick : AdminStudyGroupCreateAction
    data object OnBackClick : AdminStudyGroupCreateAction
}