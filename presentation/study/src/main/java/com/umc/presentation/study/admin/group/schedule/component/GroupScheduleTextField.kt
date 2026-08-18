package com.umc.presentation.study.admin.group.schedule.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.umc.component.component.UText
import com.umc.component.theme.*
import com.umc.component.theme.UmcTypographyTokens.Subheadline

/**
 * 스터디명 등 한 줄 텍스트를 입력하는 공통 입력창
 */
@Composable
fun GroupScheduleTextField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = Subheadline.copy(
            color = grey800()
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(
                grey000(),
                RoundedCornerShape(8.dp)
            )
            .border(
                1.dp,
                grey300(),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 16.dp),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.CenterStart,
            ) {
                // 입력값이 없을 때 placeholder 표시
                if (value.isBlank()) {
                    UText(
                        text = placeholder,
                        style = Subheadline,
                        color = grey400(),
                    )
                }

                innerTextField()
            }
        },
    )
}