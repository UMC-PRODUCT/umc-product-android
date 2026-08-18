package com.umc.presentation.study.admin.submit.component

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 관리자 제출 현황 화면의 필터 영역
 *
 * 조회할 주차와 스터디 그룹을 선택할 수 있는
 * 두 개의 드롭다운을 표시합니다.
 */
@Composable
fun AdminSubmitFilterBar(
    selectedWeek: Int,
    selectedGroupName: String,
    onWeekClick: () -> Unit,
    onGroupClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 16.dp,
            ),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 조회할 주차 선택
        AdminSubmitDropdown(
            text = "${selectedWeek}주차",
            onClick = onWeekClick
        )

        // 조회할 스터디 그룹 선택
        AdminSubmitDropdown(
            text = selectedGroupName,
            onClick = onGroupClick
        )
    }
}