package com.umc.presentation.act

sealed interface ActAction {
    data object LoadUserInfo : ActAction
    data class SetAdminMode(val isAdmin: Boolean) : ActAction
}
