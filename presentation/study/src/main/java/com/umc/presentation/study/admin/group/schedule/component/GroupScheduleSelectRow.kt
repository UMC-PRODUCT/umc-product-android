package com.umc.presentation.study.admin.group.schedule.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.*
import com.umc.component.theme.UmcTypographyTokens.Body

@Composable
fun GroupScheduleSelectRow(
    text: String,
    placeholder: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val displayText = text.ifBlank { placeholder }
    val isPlaceholder = text.isBlank()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(grey000(), RoundedCornerShape(8.dp))
            .border(1.dp, grey300(), RoundedCornerShape(8.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UText(
            text = displayText,
            style = Body,
            color = when {
                !enabled -> grey300()
                isPlaceholder -> grey400()
                else -> grey800()
            },
            modifier = Modifier.weight(1f),
        )

        Icon(
            painter = painterResource(R.drawable.ic_arrow_next),
            contentDescription = null,
            tint = if (enabled) grey500() else grey300(),
            modifier = Modifier.size(14.dp),
        )
    }
}