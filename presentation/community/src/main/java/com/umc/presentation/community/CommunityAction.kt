package com.umc.presentation.community

import com.umc.presentation.community.model.CommunityCategory

sealed interface CommunityAction {

    data class OnCategorySelected(
        val category: CommunityCategory,
    ) : CommunityAction

    data class OnThreadClick(
        val threadId: String,
    ) : CommunityAction

    data object OnFilterClick : CommunityAction

    data object OnSearchClick : CommunityAction

    data object OnCreateThreadClick : CommunityAction

    data object OnRetryClick : CommunityAction


    data class OnThreadLongClick(val threadId: String) : CommunityAction
    data object OnDismissThreadMenu : CommunityAction
    data object OnTogglePinClick : CommunityAction
    data object OnToggleNotificationClick : CommunityAction
    data object OnEditThreadClick : CommunityAction
    data object OnLeaveThreadClick : CommunityAction
    data object OnDismissLeaveDialog : CommunityAction
    data object OnConfirmLeaveClick : CommunityAction

}