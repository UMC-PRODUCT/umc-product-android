package com.umc.presentation.community.edit

sealed interface CommunityEditEvent {

    data object NavigateBack : CommunityEditEvent

    data object SaveSuccess : CommunityEditEvent

    data object DeleteSuccess : CommunityEditEvent

    data object NavigateToEmojiPicker : CommunityEditEvent

    data class ShowToast(
        val message: String,
    ) : CommunityEditEvent
}