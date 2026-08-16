package com.umc.presentation.act

import com.umc.component.base.UiState
import com.umc.domain.model.UserInfo

data class ActUiState(
    val userInfo: UserInfo = UserInfo(),
    val isAdmin: Boolean = false,
    val hasAdminAccess: Boolean = false,
) : UiState
