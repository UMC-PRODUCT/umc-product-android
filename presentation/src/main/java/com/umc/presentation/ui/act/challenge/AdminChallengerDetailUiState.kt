package com.umc.presentation.ui.act.challenge

import com.umc.domain.model.act.challenger.ChallengerManageDialogModel
import com.umc.domain.model.enums.PointType
import com.umc.presentation.base.UiState

data class AdminChallengerDetailUiState(
    val challengedId: Long = -1L,

    val isLoading: Boolean = false,
    val model: ChallengerManageDialogModel? = null,
    val currentInputMode: PointType? = null,
    val inputReason: String = "",
    val isEditMode: Boolean = false
) : UiState {
    val hasModel: Boolean get() = model != null
    val isConfirmEnabled: Boolean get() = inputReason.isNotBlank()
}