package com.umc.presentation.study.admin.submit.component

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
        AdminSubmitDropdown(
            text = "${selectedWeek}주차",
            onClick = onWeekClick
        )
        AdminSubmitDropdown(
            text = selectedGroupName,
            onClick = onGroupClick
        )
    }
}