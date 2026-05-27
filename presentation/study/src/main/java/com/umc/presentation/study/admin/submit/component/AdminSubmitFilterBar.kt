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
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
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