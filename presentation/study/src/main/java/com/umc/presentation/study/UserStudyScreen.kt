package com.umc.presentation.study

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.umc.component.theme.neutral100
import com.umc.domain.model.enums.UserPart
import com.umc.presentation.study.component.StudyCurriculumCard
import com.umc.presentation.study.component.StudyEmptyCard
import com.umc.presentation.study.component.StudyItemRow
import kotlinx.coroutines.flow.collectLatest

@Composable
fun UserStudyRoute(
    viewModel: UserStudyViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UserStudyEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    UserStudyScreen(
        state = state,
        onToggle = { index -> viewModel.toggleExpand(index) },
        onSubmitClick = { id, link -> viewModel.onSubmitClick(id, link) },
        onConfirmClick = { id -> viewModel.onConfirmClick(id) },
    )
}

@Composable
fun UserStudyScreen(
    state: UserStudyState,
    onToggle: (Int) -> Unit,
    onSubmitClick: (Long, String) -> Unit,
    onConfirmClick: (Long) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(neutral100())
    ) {
        if (state.items.isEmpty()) {
            StudyEmptyCard()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    StudyCurriculumCard(
                        part = state.part,
                        title = state.title,
                        percentText = state.percentText,
                        progress = state.progress,
                        subText = state.subText,
                    )
                }
                itemsIndexed(
                    items = state.items,
                    key = { _, item -> item.id }
                ) { index, item ->
                    StudyItemRow(
                        item = item,
                        onToggle = { onToggle(index) },
                        onSubmitClick = onSubmitClick,
                        onConfirmClick = onConfirmClick,
                    )
                }
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
private fun UserStudyScreenPreview() {
    UserStudyScreen(
        state = UserStudyState(
            title = "웹 프론트엔드 기초",
            part = UserPart.WEB,
            items = emptyList()
        ),
        onToggle = {},
        onSubmitClick = { _, _ -> },
        onConfirmClick = {},
    )
}