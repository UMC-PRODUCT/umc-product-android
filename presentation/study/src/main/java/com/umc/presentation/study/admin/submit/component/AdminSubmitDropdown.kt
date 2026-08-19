package com.umc.presentation.study.admin.submit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.*
import com.umc.component.theme.UmcTypographyTokens.Callout
import com.umc.component.theme.UmcTypographyTokens.CalloutBold
import com.umc.component.theme.UmcTypographyTokens.Caption1Bold

/**
 * 관리자 제출 현황 화면에서 사용하는 드롭다운 버튼
 *
 * 현재 선택된 주차 또는 스터디 그룹을 표시하며,
 * 클릭 시 해당 선택 BottomSheet를 엽니다.
 */
@Composable
fun AdminSubmitDropdown(
    text: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .background(grey100(), RoundedCornerShape(1000.dp))
            .clickable(onClick = onClick)
            .padding(
                start = 16.dp,
                end = 12.dp,
                top = 4.dp,
                bottom = 4.dp,
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UText(
            text = text,
            style = CalloutBold,
            color = grey500()
        )

        Spacer(modifier = Modifier.width(4.dp))

        Icon(
            painter = painterResource(R.drawable.ic_dropdown_down),
            contentDescription = null,
            tint = grey500(),
            modifier = Modifier.size(24.dp)
        )
    }
}