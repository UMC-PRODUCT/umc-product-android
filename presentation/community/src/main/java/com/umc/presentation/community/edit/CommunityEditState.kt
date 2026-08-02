package com.umc.presentation.community.edit

import com.umc.presentation.community.model.CommunityAiState
import com.umc.presentation.community.model.CommunityCategory
import com.umc.presentation.community.model.CommunityChallengerUiModel

data class CommunityEditState(
    val threadId: String = "",

    val title: String = "",
    val description: String = "",

    val selectedChallengers: List<CommunityChallengerUiModel> = emptyList(),
    val maxChallengerCount: Int = 8,

    val aiState: CommunityAiState = CommunityAiState.SUCCESS,
    val classifiedCategory: CommunityCategory? = CommunityCategory.STUDY,

    // 추가
    val selectedIcon: String = "BOOK",

    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,

    val errorMessage: String? = null,

    val showChallengerBottomSheet: Boolean = false,
    val showDeleteDialog: Boolean = false,
) {
    val selectedChallengerCountText: String
        get() = "${selectedChallengers.size} / $maxChallengerCount"

    val isSaveEnabled: Boolean
        get() = title.isNotBlank() &&
                description.isNotBlank() &&
                classifiedCategory != null &&
                !isSaving

    val canRequestClassification: Boolean
        get() = title.isNotBlank() &&
                description.isNotBlank() &&
                aiState != CommunityAiState.LOADING
}