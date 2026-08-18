package com.umc.presentation.study.admin.group.schedule.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.umc.component.component.UText
import com.umc.component.theme.*
import com.umc.component.theme.UmcTypographyTokens.Body

/**
 * 대면 진행, 출석부 생성 등
 * Boolean 옵션을 변경하는 공통 토글 Row
 */
@Composable
fun GroupScheduleToggleRow(
    checked: Boolean,
    text: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 옵션 ON / OFF 스위치
        Switch(
            checked = checked,
            onCheckedChange = {
                onClick()
            },
        )

        Spacer(
            modifier = Modifier.width(8.dp)
        )

        // 옵션 설명
        UText(
            text = text,
            style = Body,
            color = grey800(),
        )
    }
}