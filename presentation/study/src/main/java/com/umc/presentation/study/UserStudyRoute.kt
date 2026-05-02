package com.umc.presentation.study

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * 스터디/활동 화면 진입점 (Route)
 *
 * - ViewModel 주입 및 상태 수집
 * - 네비게이션에서 이 함수를 호출해서 화면 진입
 * - UI 로직은 UserStudyScreen으로 위임
 *
 */
@Composable
fun UserStudyRoute(
    viewModel: UserStudyViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    UserStudyScreen(
        state = state,
        onToggle = { index -> viewModel.toggleExpand(index) },
        onSubmitClick = { id, link -> viewModel.onSubmitClick(id, link) },
        onConfirmClick = { id -> viewModel.onConfirmClick(id) },
    )
}