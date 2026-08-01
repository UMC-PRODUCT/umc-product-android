package com.umc.presentation.community.edit

import com.umc.presentation.community.model.CommunityChallengerUiModel

sealed interface CommunityEditAction {

    data object OnBackClick : CommunityEditAction

    data object OnSaveClick : CommunityEditAction

    data object OnChallengerCardClick : CommunityEditAction

    data object OnDismissChallengerBottomSheet : CommunityEditAction

    data class OnTitleChanged(
        val title: String,
    ) : CommunityEditAction

    data class OnDescriptionChanged(
        val description: String,
    ) : CommunityEditAction

    data class OnChallengersSelected(
        val challengers: List<CommunityChallengerUiModel>,
    ) : CommunityEditAction

    data object OnRetryClassificationClick : CommunityEditAction

    data object OnChangeEmojiClick : CommunityEditAction

    data object OnDeleteThreadClick : CommunityEditAction

    data object OnDismissDeleteDialog : CommunityEditAction

    data object OnConfirmDeleteClick : CommunityEditAction
}