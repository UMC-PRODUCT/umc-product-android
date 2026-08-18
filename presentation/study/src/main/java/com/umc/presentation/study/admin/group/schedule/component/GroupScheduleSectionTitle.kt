package com.umc.presentation.study.admin.group.schedule.component

import androidx.compose.runtime.Composable
import com.umc.component.component.UText
import com.umc.component.theme.UmcTypographyTokens.HeadlineBold
import com.umc.component.theme.grey900

/**
 * 일정 등록 화면의 각 입력 영역 제목
 */
@Composable
fun GroupScheduleSectionTitle(
    text: String,
) {
    UText(
        text = text,
        style = HeadlineBold,
        color = grey900(),
    )
}