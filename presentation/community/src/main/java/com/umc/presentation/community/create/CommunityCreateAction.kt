package com.umc.presentation.community.create

import com.umc.presentation.community.model.CommunityChallengerUiModel

sealed interface CommunityCreateAction {

    data object OnBackClick : CommunityCreateAction

    data object OnCompleteClick : CommunityCreateAction

    data object OnChallengerCardClick : CommunityCreateAction

    data object OnDismissChallengerBottomSheet : CommunityCreateAction

    data class OnTitleChanged(
        val title: String,
    ) : CommunityCreateAction

    data class OnDescriptionChanged(
        val description: String,
    ) : CommunityCreateAction

    data class OnChallengersSelected(
        val challengers: List<CommunityChallengerUiModel>,
    ) : CommunityCreateAction

    data object OnRequestClassificationClick : CommunityCreateAction

    data object OnRetryClassificationClick : CommunityCreateAction

    data object OnChangeEmojiClick : CommunityCreateAction
}