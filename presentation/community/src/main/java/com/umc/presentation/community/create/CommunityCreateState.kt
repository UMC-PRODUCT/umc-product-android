package com.umc.presentation.community.create

import com.umc.presentation.community.model.CommunityAiState
import com.umc.presentation.community.model.CommunityCategory
import com.umc.presentation.community.model.CommunityChallengerUiModel

data class CommunityCreateState(
    val title: String = "",
    val description: String = "",

    val selectedChallengers: List<CommunityChallengerUiModel> = emptyList(),
    val maxChallengerCount: Int = 8,

    val aiState: CommunityAiState = CommunityAiState.GUIDE,
    val classifiedCategory: CommunityCategory? = null,

    val isSubmitting: Boolean = false,
    val showChallengerBottomSheet: Boolean = false,
) {
    val selectedChallengerCountText: String
        get() = "${selectedChallengers.size} / $maxChallengerCount"

    val isTitleValid: Boolean
        get() = title.isNotBlank()

    val isDescriptionValid: Boolean
        get() = description.isNotBlank()

    val isCompleteEnabled: Boolean
        get() = isTitleValid &&
                isDescriptionValid &&
                !isSubmitting

    val canRequestClassification: Boolean
        get() = title.isNotBlank() &&
                description.isNotBlank() &&
                aiState != CommunityAiState.LOADING
}