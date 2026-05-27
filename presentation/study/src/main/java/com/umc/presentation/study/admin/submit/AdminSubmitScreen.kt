package com.umc.presentation.study.admin.submit

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.umc.component.theme.*
import com.umc.component.theme.UmcTypographyTokens.FootnoteBold
import com.umc.presentation.study.admin.submit.component.AdminSubmitFilterBar
import com.umc.presentation.study.admin.submit.component.AdminSubmitItem
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AdminSubmitRoute(
    viewModel: AdminSubmitViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is AdminSubmitEvent.ShowToast ->
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    AdminSubmitScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun AdminSubmitScreen(
    state: AdminSubmitState,
    onAction: (AdminSubmitAction) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(neutral000())
    ) {
        AdminSubmitFilterBar(
            selectedWeek = state.selectedWeek,
            selectedGroupName = state.selectedGroupName,
            onWeekClick = { /* TODO: 바텀시트 */ },
            onGroupClick = { /* TODO: 바텀시트 */ },
        )

        if (state.items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "제출 내역이 없어요",
                    style = FootnoteBold,
                    color = neutral500()
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(neutral100()),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.items, key = { it.id }) { item ->
                    AdminSubmitItem(item = item)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AdminSubmitScreenPreview() {
    AdminSubmitScreen(
        state = AdminSubmitState(
            items = listOf(
                AdminSubmitItemUiModel(
                    id = 1L,
                    name = "홍길동",
                    nickname = "닉네임",
                    partLabel = "iOS",
                    weekText = "1주차",
                    studyTitle = "SwiftUI 클론 코딩",
                    schoolName = "중앙대",
                    status = "BEST",
                )
            )
        )
    )
}