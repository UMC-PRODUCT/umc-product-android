package com.umc.presentation.study


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.umc.component.theme.neutral100
import com.umc.domain.model.enums.UserPart
import com.umc.presentation.study.component.StudyCurriculumCard
import com.umc.presentation.study.component.StudyEmptyCard
import com.umc.presentation.study.component.StudyItemRow

/**
 * 스터디/활동 화면 전체 레이아웃
 *
 * - 상단: 커리큘럼 카드 (달성률, 프로그레스바)
 * - 중단: 주차별 스터디 아이템 리스트
 * - 빈 상태: 아이템 없을 때 안내 카드 표시
 *
 * @param state UI 상태
 * @param onToggle 카드 펼치기/접기 콜백 (index)
 * @param onSubmitClick 링크 제출 콜백 (itemId, link)
 * @param onConfirmClick 학습 완료 인증 콜백 (itemId)
 */
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
            .background(neutral100()) // 배경색
    ) {
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

            if (state.items.isEmpty()) {
                item { StudyEmptyCard() }
            } else {
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