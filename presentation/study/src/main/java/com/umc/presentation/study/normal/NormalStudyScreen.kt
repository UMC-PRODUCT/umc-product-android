package com.umc.presentation.study.normal

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.umc.component.theme.grey100
import com.umc.domain.model.enums.UserPart
import com.umc.presentation.study.normal.component.StudyCurriculumCard
import com.umc.presentation.study.normal.component.StudyEmptyCard
import com.umc.presentation.study.normal.component.StudyItemRow
import kotlinx.coroutines.flow.collectLatest
import com.umc.domain.model.enums.StudyStatus

/**
 * 일반 사용자 스터디 화면의 Route
 *
 * ViewModel 상태를 구독하고 Toast 이벤트를 처리한 뒤,
 * 실제 스터디 화면에 상태와 이벤트를 전달합니다.
 */
@Composable
fun UserStudyRoute(
    viewModel: UserStudyViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // ViewModel에서 발생한 일회성 Toast 이벤트 처리
    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UserStudyEvent.ShowToast -> {
                    Toast.makeText(
                        context,
                        event.message,
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        }
    }

    UserStudyScreen(
        state = state,
        onToggle = viewModel::toggleExpand,
    )
}

/**
 * 일반 사용자의 스터디 커리큘럼 화면
 *
 * 전체 진행률 카드와 주차별 커리큘럼 목록을 표시하며,
 * 데이터가 없을 경우 Empty 화면을 표시합니다.
 */
@Composable
fun UserStudyScreen(
    state: UserStudyState,
    onToggle: (Int) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(grey100())
            .padding(
                horizontal = 16.dp,
                vertical = 12.dp,
            )
    ) {
        // 조회된 커리큘럼이 없을 경우 Empty 화면 표시
        if (state.items.isEmpty()) {
            StudyEmptyCard(
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            // 전체 진행률 카드와 주차별 커리큘럼 목록
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    bottom = 16.dp,
                ),
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
                    key = { _, item -> item.id },
                ) { index, item ->
                    StudyItemRow(
                        item = item,
                        onToggle = {
                            onToggle(index)
                        }
                    )
                }
            }
        }
    }
}

/**
 * 일반 사용자 스터디 화면 Preview
 */
@Preview(showBackground = true)
@Composable
private fun UserStudyScreenPreview() {
    UserStudyScreen(
        state = UserStudyState(
            title = "웹 프론트엔드 기초",
            part = UserPart.WEB,
            items = listOf(
                NormalStudyItemUiModel(
                    id = 1,
                    week = 1,
                    title = "HTML/CSS 기초",
                    description = "HTML/CSS 기초를 학습합니다.",
                    platform = "Github",
                    status = StudyStatus.PASS,
                    isBest = false,
                ),
                NormalStudyItemUiModel(
                    id = 2,
                    week = 2,
                    title = "HTML/CSS 심화",
                    description = "Flex와 Grid를 학습합니다.",
                    platform = "Github",
                    status = StudyStatus.FAIL,
                    isExpanded = true,
                ),
                NormalStudyItemUiModel(
                    id = 3,
                    week = 3,
                    title = "Javascript 기초",
                    description = "Javascript 문법을 학습합니다.",
                    platform = "Github",
                    status = StudyStatus.IN_PROGRESS,
                ),
                NormalStudyItemUiModel(
                    id = 4,
                    week = 4,
                    title = "React 기초",
                    description = "React를 학습합니다.",
                    platform = "Github",
                    status = StudyStatus.IN_PROGRESS,
                    isLocked = true,
                ),
            ),
        ),
        onToggle = {},
    )
}