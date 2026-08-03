package com.umc.presentation.community.create

sealed interface CommunityCreateEvent {

    data object NavigateBack : CommunityCreateEvent

    data class CreateSuccess(
        val threadId: String,
    ) : CommunityCreateEvent

    data object NavigateToEmojiPicker : CommunityCreateEvent

    data class ShowToast(
        val message: String,
    ) : CommunityCreateEvent
}