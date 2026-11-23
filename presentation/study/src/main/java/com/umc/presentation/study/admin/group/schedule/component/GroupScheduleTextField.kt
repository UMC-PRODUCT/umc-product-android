package com.umc.presentation.study.admin.group.schedule.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.umc.component.component.UTextField
import com.umc.component.theme.UmcTypographyTokens.Subheadline
import com.umc.component.theme.grey300

/**
 * 스터디명 등 한 줄 텍스트를 입력하는 공통 입력창
 */
@Composable
fun GroupScheduleTextField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
) {
    // 기존 모양(가로 여백 16dp, 세로 여백 없음, 포커스 시 테두리 유지, 검정 커서)을 그대로 둔다.
    // 52dp 높이에서 기본 세로 여백 14dp 를 두면 글자 크기를 키웠을 때 입력 글자가 잘리므로 세로 여백은 0 으로 둔다.
    UTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        textStyle = Subheadline,
        focusStrokeColor = grey300(),
        cursorColor = Color.Black,
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
    )
}
