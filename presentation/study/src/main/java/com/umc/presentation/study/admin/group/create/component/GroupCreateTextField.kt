package com.umc.presentation.study.admin.group.create.component

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.umc.component.component.UText
import com.umc.component.component.UTextField
import com.umc.component.theme.*

/**
 * 스터디 그룹 생성 화면에서
 * 직접 텍스트를 입력하는 공통 입력 컴포넌트
 *
 * 현재 스터디 그룹 이름 입력 등에 사용합니다.
 *
 * 주요 기능
 * - 입력 항목 제목 표시
 * - 현재 입력값 표시
 * - 입력값 변경 이벤트 전달
 * - 값이 없을 경우 placeholder 표시
 */
@Composable
fun GroupCreateTextField(
    title: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 입력 항목 제목
        UText(
            text = title,
            style = UmcTypographyTokens.HeadlineBold,
            color = grey800()
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        // 텍스트 입력 영역
        UTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        )
    }
}