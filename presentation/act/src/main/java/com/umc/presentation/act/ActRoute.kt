package com.umc.presentation.act

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ActManageRoute(
    vm: ActViewModel = hiltViewModel(),
    onNavigateToChallengerDetail: (Long) -> Unit = {},
) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(vm) {
        vm.onAction(ActAction.LoadUserInfo)
    }

    ActManageScreen(
        uiState = uiState,
        onAdminCheckedChange = { vm.onAction(ActAction.SetAdminMode(it)) },
        onNavigateToChallengerDetail = onNavigateToChallengerDetail,
    )
}
