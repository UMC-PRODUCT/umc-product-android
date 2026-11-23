package com.umc.presentation.study.admin.group.schedule.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.umc.component.component.UTextField
import com.umc.component.theme.UmcTypographyTokens.Body
import com.umc.component.theme.grey300

/**
 * 스터디 일정의 상세 안내를 입력하는 멀티라인 입력창
 */
@Composable
fun GroupScheduleMemoBox(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
) {
    // 기존 모양(사방 여백 16dp, 위쪽 정렬, 포커스 시 테두리 유지, 검정 커서)을 그대로 둔다.
    UTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        textStyle = Body,
        focusStrokeColor = grey300(),
        cursorColor = Color.Black,
        contentPadding = PaddingValues(16.dp),
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
    )
}
