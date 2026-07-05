package com.umc.presentation.study.admin.group.schedule.component

import androidx.compose.runtime.Composable
import com.umc.component.component.UText
import com.umc.component.theme.UmcTypographyTokens.HeadlineBold
import com.umc.component.theme.neutral900

@Composable
fun GroupScheduleSectionTitle(
    text: String,
) {
    UText(
        text = text,
        style = HeadlineBold,
        color = neutral900(),
    )
}