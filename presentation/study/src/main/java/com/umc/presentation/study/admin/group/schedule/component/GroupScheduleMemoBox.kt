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
import com.umc.component.theme.UmcTypographyTokens.Body

/**
 * 스터디 일정의 상세 안내를 입력하는 멀티라인 입력창
 */
@Composable
fun GroupScheduleMemoBox(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = Body.copy(
            color = grey800()
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(
                grey000(),
                RoundedCornerShape(8.dp)
            )
            .border(
                1.dp,
                grey300(),
                RoundedCornerShape(8.dp)
            )
            .padding(
                horizontal = 16.dp,
                vertical = 16.dp
            ),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopStart,
            ) {
                // 입력값이 없을 때 안내 문구 표시
                if (value.isBlank()) {
                    UText(
                        text = placeholder,
                        style = Body,
                        color = grey400(),
                    )
                }

                innerTextField()
            }
        },
    )
}