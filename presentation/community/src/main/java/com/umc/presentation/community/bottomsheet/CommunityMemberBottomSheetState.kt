package com.umc.presentation.community.bottomsheet

import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.presentation.community.model.CommunityInvitableMemberUiModel

data class CommunityMemberBottomSheetState(
    val threadId: String = "",
    val query: String = "",
    val isSearching: Boolean = false,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isInviting: Boolean = false,
    val invitableMembers: List<CommunityInvitableMemberUiModel> = emptyList(),
    val searchResults: List<CommunityInvitableMemberUiModel> = emptyList(),
    val selectedMembers: List<CommunityInvitableMemberUiModel> = emptyList(),
    val nextOffset: Int? = null,
    val hasNext: Boolean = false,
    val total: Int = 0,
    val maxMemberCount: Int = 8,
    val errorMessage: String? = null,
) : UiState {

    val displayedMembers: List<CommunityInvitableMemberUiModel>
        get() = searchResults

    val isEmpty: Boolean
        get() = !isLoading && displayedMembers.isEmpty()

    val isConfirmEnabled: Boolean
        get() = selectedMembers.isNotEmpty() && !isInviting

    val selectedCountText: String
        get() = "${selectedMembers.size}/$maxMemberCount"
}

sealed interface CommunityMemberBottomSheetEvent : UiEvent {

    data class InviteSuccess(
        val invitedMemberCount: Int,
        val totalMemberCount: Int,
    ) : CommunityMemberBottomSheetEvent

    data class ShowToast(
        val message: String,
    ) : CommunityMemberBottomSheetEvent
}